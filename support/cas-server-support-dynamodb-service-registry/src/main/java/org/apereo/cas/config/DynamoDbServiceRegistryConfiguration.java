package org.apereo.cas.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.dynamodb.DynamoDbServiceRegistryProperties;
import org.apereo.cas.services.DynamoDbServiceRegistryDao;
import org.apereo.cas.services.DynamoDbServiceRegistryFacilitator;
import org.apereo.cas.services.ServiceRegistryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;

/**
 * This is {@link DynamoDbServiceRegistryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("dynamoDbServiceRegistryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DynamoDbServiceRegistryConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public DynamoDbServiceRegistryFacilitator dynamoDbServiceRegistryFacilitator() {
        final DynamoDbServiceRegistryProperties db = casProperties.getServiceRegistry().getDynamoDb();
        return new DynamoDbServiceRegistryFacilitator(db, amazonDynamoDbClient());
    }

    @Bean
    public ServiceRegistryDao serviceRegistryDao() {
        return new DynamoDbServiceRegistryDao(dynamoDbServiceRegistryFacilitator());
    }

    @RefreshScope
    @Bean
    public AmazonDynamoDBClient amazonDynamoDbClient() {
        try {
            final DynamoDbServiceRegistryProperties dynamoDbProperties = casProperties.getServiceRegistry().getDynamoDb();
            final ClientConfiguration cfg = new ClientConfiguration();
            cfg.setConnectionTimeout(dynamoDbProperties.getConnectionTimeout());
            cfg.setMaxConnections(dynamoDbProperties.getMaxConnections());
            cfg.setRequestTimeout(dynamoDbProperties.getRequestTimeout());
            cfg.setSocketTimeout(dynamoDbProperties.getSocketTimeout());
            cfg.setUseGzip(dynamoDbProperties.isUseGzip());
            cfg.setUseReaper(dynamoDbProperties.isUseReaper());
            cfg.setUseThrottleRetries(dynamoDbProperties.isUseThrottleRetries());
            cfg.setUseTcpKeepAlive(dynamoDbProperties.isUseTcpKeepAlive());
            cfg.setProtocol(Protocol.valueOf(dynamoDbProperties.getProtocol().toUpperCase()));
            cfg.setClientExecutionTimeout(dynamoDbProperties.getClientExecutionTimeout());
            cfg.setCacheResponseMetadata(dynamoDbProperties.isCacheResponseMetadata());

            if (StringUtils.isNotBlank(dynamoDbProperties.getLocalAddress())) {
                cfg.setLocalAddress(InetAddress.getByName(dynamoDbProperties.getLocalAddress()));
            }

            AWSCredentials credentials = null;
            if (dynamoDbProperties.getCredentialsPropertiesFile() != null) {
                credentials = new PropertiesCredentials(dynamoDbProperties.getCredentialsPropertiesFile().getInputStream());
            } else if (StringUtils.isNotBlank(dynamoDbProperties.getCredentialAccessKey())
                    && StringUtils.isNotBlank(dynamoDbProperties.getCredentialSecretKey())) {
                credentials = new BasicAWSCredentials(dynamoDbProperties.getCredentialAccessKey(), dynamoDbProperties.getCredentialSecretKey());
            }

            final AmazonDynamoDBClient client;
            if (credentials == null) {
                client = new AmazonDynamoDBClient(cfg);
            } else {
                client = new AmazonDynamoDBClient(credentials, cfg);
            }

            if (StringUtils.isNotBlank(dynamoDbProperties.getEndpoint())) {
                client.setEndpoint(dynamoDbProperties.getEndpoint());
            }

            if (StringUtils.isNotBlank(dynamoDbProperties.getRegion())) {
                client.setRegion(Region.getRegion(Regions.valueOf(dynamoDbProperties.getRegion())));
            }

            if (StringUtils.isNotBlank(dynamoDbProperties.getRegionOverride())) {
                client.setSignerRegionOverride(dynamoDbProperties.getRegionOverride());
            }

            if (StringUtils.isNotBlank(dynamoDbProperties.getServiceNameIntern())) {
                client.setServiceNameIntern(dynamoDbProperties.getServiceNameIntern());
            }

            if (dynamoDbProperties.getTimeOffset() != 0) {
                client.setTimeOffset(dynamoDbProperties.getTimeOffset());
            }

            return client;
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
