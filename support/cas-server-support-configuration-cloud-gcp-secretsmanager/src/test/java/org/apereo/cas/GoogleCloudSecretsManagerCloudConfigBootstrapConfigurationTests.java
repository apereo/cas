package org.apereo.cas;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.cloud.spring.core.DefaultGcpProjectIdProvider;
import com.google.cloud.spring.secretmanager.SecretManagerTemplate;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link GoogleCloudSecretsManagerCloudConfigBootstrapConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("CasConfiguration")
@Slf4j
class GoogleCloudSecretsManagerCloudConfigBootstrapConfigurationTests {
    @Nested
    class DefaultTests extends BaseGoogleCloudSecretsManagerTests {
        @Test
        void verifyOperation() {
            val source = propertySourceLocator.locate(environment);
            val propertyValue = source.getProperty("sm://projects/1234567890/secrets/cas_authn_accept_users");
            assertNull(propertyValue);
        }
    }

    @Nested
    @Import(MockTests.GoogleCloudSecretsManagerTestConfiguration.class)
    class MockTests extends BaseGoogleCloudSecretsManagerTests {
        @Test
        void verifyOperation() {
            val source = propertySourceLocator.locate(environment);
            val propertyValue = source.getProperty("sm://projects/1234567890/secrets/cas_authn_accept_users");
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
