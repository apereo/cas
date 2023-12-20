package org.apereo.cas.aws;

import org.apereo.cas.configuration.model.support.dynamodb.AbstractDynamoDbProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.awscore.client.builder.AwsSyncClientBuilder;

import java.io.Serial;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AmazonClientConfigurationBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("AmazonWebServices")
class AmazonClientConfigurationBuilderTests {

    @Test
    void verifyOperation() throws Throwable {
        val properties = new AbstractDynamoDbProperties() {
            @Serial
            private static final long serialVersionUID = -3599433486448467450L;
        };
        properties.setLocalAddress("github.com");
        properties.setEndpoint("http://localhost:4532");
        properties.setProxyHost("http://localhost:8080");

        val httpClientBuilder = mock(SampleClientBuilder.class);
        val clientBuilder = mock(AwsSyncClientBuilder.class);
        when(clientBuilder.httpClientBuilder(any())).thenReturn(httpClientBuilder);
        assertDoesNotThrow(() -> AmazonClientConfigurationBuilder.prepareSyncClientBuilder(clientBuilder,
            StaticCredentialsProvider.create(AwsBasicCredentials.create("key", "secret")), properties));
    }

    private interface SampleClientBuilder extends AwsClientBuilder, AwsSyncClientBuilder {
    }
}
