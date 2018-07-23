package org.apereo.cas.dynamodb;

import org.apereo.cas.aws.ChainingAWSCredentialsProvider;
import org.apereo.cas.configuration.model.support.dynamodb.AbstractDynamoDbProperties;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;

/**
 * This is {@link AmazonDynamoDbClientFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class AmazonDynamoDbClientFactory {

    /**
     * Create amazon dynamo db instance.
     *
     * @param dynamoDbProperties the dynamo db properties
     * @return the amazon dynamo db
     */
    @SneakyThrows
    public AmazonDynamoDB createAmazonDynamoDb(final AbstractDynamoDbProperties dynamoDbProperties) {
        if (dynamoDbProperties.isLocalInstance()) {
            LOGGER.debug("Creating DynamoDb standard client with endpoint [{}] and region [{}]",
                dynamoDbProperties.getEndpoint(), dynamoDbProperties.getRegion());
            val endpoint = new AwsClientBuilder.EndpointConfiguration(
                dynamoDbProperties.getEndpoint(), dynamoDbProperties.getRegion());
            return AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(endpoint)
                .build();
        }

        val provider =
            ChainingAWSCredentialsProvider.getInstance(dynamoDbProperties.getCredentialAccessKey(),
                dynamoDbProperties.getCredentialSecretKey(), dynamoDbProperties.getCredentialsPropertiesFile());

        LOGGER.debug("Creating DynamoDb client configuration...");
        val cfg = new ClientConfiguration();
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
            LOGGER.debug("Creating DynamoDb client local address [{}]", dynamoDbProperties.getLocalAddress());
            cfg.setLocalAddress(InetAddress.getByName(dynamoDbProperties.getLocalAddress()));
        }


        LOGGER.debug("Creating DynamoDb client instance...");
        val client = new AmazonDynamoDBClient(provider, cfg);

        if (StringUtils.isNotBlank(dynamoDbProperties.getEndpoint())) {
            LOGGER.debug("Setting DynamoDb client endpoint [{}]", dynamoDbProperties.getEndpoint());
            client.setEndpoint(dynamoDbProperties.getEndpoint());
        }

        if (StringUtils.isNotBlank(dynamoDbProperties.getRegion())) {
            LOGGER.debug("Setting DynamoDb client region [{}]", dynamoDbProperties.getRegion());
            client.setRegion(Region.getRegion(Regions.valueOf(dynamoDbProperties.getRegion())));
        }

        if (StringUtils.isNotBlank(dynamoDbProperties.getRegionOverride())) {
            LOGGER.debug("Setting DynamoDb client region override [{}]", dynamoDbProperties.getRegionOverride());
            client.setSignerRegionOverride(dynamoDbProperties.getRegionOverride());
        }

        if (StringUtils.isNotBlank(dynamoDbProperties.getServiceNameIntern())) {
            client.setServiceNameIntern(dynamoDbProperties.getServiceNameIntern());
        }

        if (dynamoDbProperties.getTimeOffset() != 0) {
            client.setTimeOffset(dynamoDbProperties.getTimeOffset());
        }
        return client;
    }
}
