package org.apereo.cas.dynamodb;

import org.apereo.cas.aws.AmazonClientConfigurationBuilder;
import org.apereo.cas.aws.ChainingAWSCredentialsProvider;
import org.apereo.cas.configuration.model.support.dynamodb.AbstractDynamoDbProperties;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

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

        LOGGER.trace("Creating DynamoDb client configuration...");
        val cfg = AmazonClientConfigurationBuilder.buildClientConfiguration(props);

        LOGGER.debug("Creating DynamoDb client instance...");
        val clientBuilder = AmazonDynamoDBClientBuilder
            .standard()
            .withClientConfiguration(cfg)
            .withCredentials(provider);

        val region = StringUtils.defaultIfBlank(props.getRegionOverride(), props.getRegion());

        if (StringUtils.isNotBlank(props.getEndpoint())) {
            LOGGER.trace("Setting DynamoDb client endpoint [{}]", props.getEndpoint());
            clientBuilder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(props.getEndpoint(), region));
        }

        if (StringUtils.isNotBlank(region)) {
            LOGGER.trace("Setting DynamoDb client region [{}]", props.getRegion());
            clientBuilder.withRegion(region);
        }
        return clientBuilder.build();
    }
}
