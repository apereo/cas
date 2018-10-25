package org.apereo.cas.dynamodb;

import org.apereo.cas.aws.ChainingAWSCredentialsProvider;
import org.apereo.cas.configuration.model.support.dynamodb.AbstractDynamoDbProperties;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
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
     * @param props the dynamo db properties
     * @return the amazon dynamo db
     */
    @SneakyThrows
    public AmazonDynamoDB createAmazonDynamoDb(final AbstractDynamoDbProperties props) {
        if (props.isLocalInstance()) {
            LOGGER.debug("Creating DynamoDb standard client with endpoint [{}] and region [{}]",
                props.getEndpoint(), props.getRegion());
            val endpoint = new AwsClientBuilder.EndpointConfiguration(
                props.getEndpoint(), props.getRegion());
            return AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(endpoint)
                .build();
        }

        val provider = ChainingAWSCredentialsProvider.getInstance(props.getCredentialAccessKey(),
                props.getCredentialSecretKey(), props.getCredentialsPropertiesFile());

        LOGGER.debug("Creating DynamoDb client configuration...");
        val cfg = new ClientConfiguration();
        cfg.setConnectionTimeout(props.getConnectionTimeout());
        cfg.setMaxConnections(props.getMaxConnections());
        cfg.setRequestTimeout(props.getRequestTimeout());
        cfg.setSocketTimeout(props.getSocketTimeout());
        cfg.setUseGzip(props.isUseGzip());
        cfg.setUseReaper(props.isUseReaper());
        cfg.setUseThrottleRetries(props.isUseThrottleRetries());
        cfg.setUseTcpKeepAlive(props.isUseTcpKeepAlive());
        cfg.setProtocol(Protocol.valueOf(props.getProtocol().toUpperCase()));
        cfg.setClientExecutionTimeout(props.getClientExecutionTimeout());
        cfg.setMaxErrorRetry(props.getMaxErrorRetry());
        cfg.setProxyHost(props.getProxyHost());
        cfg.setProxyPassword(props.getProxyPassword());
        if (props.getProxyPort() > 0) {
            cfg.setProxyPort(props.getProxyPort());
        }
        cfg.setProxyUsername(props.getProxyUsername());
        cfg.setCacheResponseMetadata(props.isCacheResponseMetadata());

        if (StringUtils.isNotBlank(props.getLocalAddress())) {
            LOGGER.debug("Creating DynamoDb client local address [{}]", props.getLocalAddress());
            cfg.setLocalAddress(InetAddress.getByName(props.getLocalAddress()));
        }

        LOGGER.debug("Creating DynamoDb client instance...");
        val clientBuilder = AmazonDynamoDBClientBuilder
            .standard()
            .withClientConfiguration(cfg)
            .withCredentials(provider);

        val region = StringUtils.defaultIfBlank(props.getRegionOverride(), props.getRegion());

        if (StringUtils.isNotBlank(props.getEndpoint())) {
            LOGGER.debug("Setting DynamoDb client endpoint [{}]", props.getEndpoint());
            clientBuilder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(props.getEndpoint(), region));
        }

        if (StringUtils.isNotBlank(region)) {
            LOGGER.debug("Setting DynamoDb client region [{}]", props.getRegion());
            clientBuilder.withRegion(region);
        }
        return clientBuilder.build();
    }
}
