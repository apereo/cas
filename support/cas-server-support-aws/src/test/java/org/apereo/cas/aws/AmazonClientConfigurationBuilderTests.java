package org.apereo.cas.aws;

import org.apereo.cas.configuration.model.support.dynamodb.AbstractDynamoDbProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.awscore.client.builder.AwsSyncClientBuilder;
import software.amazon.awssdk.core.client.builder.SdkSyncClientBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AmazonClientConfigurationBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("AmazonWebServices")
public class AmazonClientConfigurationBuilderTests {

    @Test
    public void verifyOperation() {
        val properties = new AbstractDynamoDbProperties() {
            private static final long serialVersionUID = -3599433486448467450L;
        };
        properties.setLocalAddress("github.com");
        properties.setEndpoint("http://localhost:4532");
        properties.setProxyHost("http://localhost:8080");

        val httpClientBuilder = mock(SampleClientBuilder.class);
        val clientBuilder = mock(AwsSyncClientBuilder.class);
        when(clientBuilder.httpClientBuilder(any())).thenReturn(httpClientBuilder);
        assertDoesNotThrow(() -> AmazonClientConfigurationBuilder.prepareClientBuilder(clientBuilder,
            StaticCredentialsProvider.create(AwsBasicCredentials.create("key", "secret")), properties));
    }

    private interface SampleClientBuilder extends AwsClientBuilder, AwsSyncClientBuilder, SdkSyncClientBuilder {
    }
}
