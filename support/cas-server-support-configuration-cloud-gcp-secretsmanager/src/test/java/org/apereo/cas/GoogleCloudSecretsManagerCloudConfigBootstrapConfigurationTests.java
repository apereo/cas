package org.apereo.cas;

import module java.base;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.AddSecretVersionRequest;
import com.google.cloud.secretmanager.v1.CreateSecretRequest;
import com.google.cloud.secretmanager.v1.DeleteSecretRequest;
import com.google.cloud.secretmanager.v1.ProjectName;
import com.google.cloud.secretmanager.v1.Replication;
import com.google.cloud.secretmanager.v1.Secret;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.cloud.spring.core.DefaultGcpProjectIdProvider;
import com.google.cloud.spring.secretmanager.SecretManagerTemplate;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GoogleCloudSecretsManagerCloudConfigBootstrapConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("GCP")
@Slf4j
class GoogleCloudSecretsManagerCloudConfigBootstrapConfigurationTests {
    @Nested
    @Import(DefaultTests.GoogleCloudSecretsManagerTestConfiguration.class)
    @EnabledIfListeningOnPort(port = 9090)
    class DefaultTests extends BaseGoogleCloudSecretsManagerTests {
        @Test
        void verifyOperation() {
            val source = propertySourceLocator.locate(environment);
            val propertyValue = source.getProperty("sm@projects/1234567890/secrets/cas_authn_accept_users");
            assertEquals("casuser::Hello", propertyValue);
        }

        @TestConfiguration(value = "GoogleCloudSecretsManagerTestConfiguration", proxyBeanMethods = false)
        static class GoogleCloudSecretsManagerTestConfiguration {
            @Bean
            public SecretManagerTemplate googleCloudSecretsManagerTemplate(
                @Qualifier("googleCloudSecretsManagerCredentialProvider")
                final CredentialsProvider credentialsProvider) throws Exception {
                val settings = SecretManagerServiceSettings.newBuilder()
                    .setCredentialsProvider(credentialsProvider)
                    .setTransportChannelProvider(
                        InstantiatingGrpcChannelProvider.newBuilder()
                            .setEndpoint("localhost:9090")
                            .setChannelConfigurator(ManagedChannelBuilder::usePlaintext)
                            .build()
                    )
                    .build();

                val serviceClient = SecretManagerServiceClient.create(settings);

                createSecret(serviceClient, "cas_authn_accept_users", "casuser::Hello");
                createSecret(serviceClient, "cas_authn_accept_enabled", "true");

                return new SecretManagerTemplate(serviceClient, new DefaultGcpProjectIdProvider());
            }

            private static void createSecret(
                final SecretManagerServiceClient client,
                final String secretId,
                final String value) {
                val secret =
                    Secret.newBuilder()
                        .setReplication(
                            Replication.newBuilder()
                                .setAutomatic(Replication.Automatic.newBuilder().build())
                                .build()
                        )
                        .build();

                FunctionUtils.doAndHandle(_ -> {
                    client.deleteSecret(DeleteSecretRequest.newBuilder()
                        .setName("projects/1234567890/secrets/" + secretId)
                        .build());
                });

                client.createSecret(
                    CreateSecretRequest.newBuilder()
                        .setParent(ProjectName.of("1234567890").toString())
                        .setSecretId(secretId)
                        .setSecret(secret)
                        .build()
                );

                client.addSecretVersion(
                    AddSecretVersionRequest.newBuilder()
                        .setParent("projects/1234567890/secrets/" + secretId)
                        .setPayload(
                            SecretPayload.newBuilder()
                                .setData(ByteString.copyFromUtf8(value))
                                .build()
                        )
                        .build()
                );
            }
        }
    }

    @Nested
    @Import(MockTests.GoogleCloudSecretsManagerTestConfiguration.class)
    class MockTests extends BaseGoogleCloudSecretsManagerTests {
        @Test
        void verifyOperation() {
            val source = propertySourceLocator.locate(environment);
            val propertyValue = source.getProperty("sm@projects/1234567890/secrets/cas_authn_accept_users");
            assertEquals("casuser::Mellon", propertyValue);
        }

        @TestConfiguration(value = "GoogleCloudSecretsManagerTestConfiguration", proxyBeanMethods = false)
        static class GoogleCloudSecretsManagerTestConfiguration {
            @Bean
            public SecretManagerTemplate googleCloudSecretsManagerTemplate() {
                val secretResponse = AccessSecretVersionResponse
                    .newBuilder()
                    .setName("cas.authn.accept.users")
                    .setPayload(SecretPayload.newBuilder()
                        .setData(ByteString.copyFrom("casuser::Mellon".getBytes(StandardCharsets.UTF_8)))
                        .build())
                    .build();
                val serviceClient = mock(SecretManagerServiceClient.class);
                when(serviceClient.accessSecretVersion(any(SecretVersionName.class))).thenReturn(secretResponse);
                return new SecretManagerTemplate(serviceClient, new DefaultGcpProjectIdProvider());
            }
        }
    }
}
