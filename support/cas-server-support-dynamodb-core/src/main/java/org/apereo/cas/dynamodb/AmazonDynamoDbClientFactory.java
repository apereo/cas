package org.apereo.cas.dynamodb;

import org.apereo.cas.aws.AmazonClientConfigurationBuilder;
import org.apereo.cas.aws.ChainingAWSCredentialsProvider;
import org.apereo.cas.configuration.model.support.dynamodb.AbstractDynamoDbProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.dax.ClusterDaxClient;
import software.amazon.dax.Configuration;

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
    public DynamoDbClient createAmazonDynamoDb(final AbstractDynamoDbProperties props) {
        if (props.isLocalInstance()) {
            LOGGER.debug("Creating DynamoDb standard client with endpoint [{}] and region [{}]",
                props.getEndpoint(), props.getRegion());
            return DynamoDbClient.builder()
                .endpointOverride(FunctionUtils.doUnchecked(() -> new URI(props.getEndpoint())))
                .region(Region.of(props.getRegion()))
                .build();
        }

        val provider = ChainingAWSCredentialsProvider.getInstance(props.getCredentialAccessKey(), props.getCredentialSecretKey());
        return createDynamoDbClient(provider, props);
    }

    protected DynamoDbClient createDynamoDbClient(final AwsCredentialsProvider credentialsProvider,
                                                  final AbstractDynamoDbProperties props) {
        LOGGER.trace("Creating DynamoDb client configuration...");
        if (StringUtils.isNotBlank(props.getDax().getUrl())) {
            return FunctionUtils.doUnchecked(() -> {
                val region = StringUtils.isBlank(props.getRegion()) ? Region.AWS_GLOBAL : Region.of(props.getRegion());
                val configuration = Configuration.builder()
                    .region(region)
                    .url(props.getDax().getUrl())
                    .credentialsProvider(credentialsProvider)
                    .requestTimeoutMillis(Math.toIntExact(Beans.newDuration(props.getDax().getRequestTimeout()).toMillis()))
                    .connectTimeoutMillis(Math.toIntExact(Beans.newDuration(props.getDax().getConnectTimeout()).toMillis()))
                    .idleTimeoutMillis(Math.toIntExact(Beans.newDuration(props.getDax().getIdleTimeout()).toMillis()))
                    .connectionTtlMillis(Math.toIntExact(Beans.newDuration(props.getDax().getConnectionTtl()).toMillis()))
                    .maxConcurrency(props.getDax().getMaxConcurrency())
                    .readRetries(props.getDax().getReadRetries())
                    .writeRetries(props.getDax().getWriteRetries())
                    .build();
                return ClusterDaxClient
                    .builder()
                    .overrideConfiguration(configuration)
                    .build();
            });
        }
        val builder = DynamoDbClient.builder();
        AmazonClientConfigurationBuilder.prepareSyncClientBuilder(builder, credentialsProvider, props);
        return builder.build();
    }
}
