package org.apereo.cas.dynamodb;

import org.apereo.cas.aws.AmazonClientConfigurationBuilder;
import org.apereo.cas.aws.ChainingAWSCredentialsProvider;
import org.apereo.cas.configuration.model.support.dynamodb.AbstractDynamoDbProperties;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

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
    public DynamoDbClient createAmazonDynamoDb(final AbstractDynamoDbProperties props) {
        if (props.isLocalInstance()) {
            LOGGER.debug("Creating DynamoDb standard client with endpoint [{}] and region [{}]",
                props.getEndpoint(), props.getRegion());
            return DynamoDbClient.builder()
                .endpointOverride(new URI(props.getEndpoint()))
                .region(Region.of(props.getRegion()))
                .build();
        }

        val provider = ChainingAWSCredentialsProvider.getInstance(props.getCredentialAccessKey(), props.getCredentialSecretKey());
        LOGGER.trace("Creating DynamoDb client configuration...");
        val builder = DynamoDbClient.builder();
        AmazonClientConfigurationBuilder.prepareClientBuilder(builder, provider, props);
        return builder.build();
    }
}
