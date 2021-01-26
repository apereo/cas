package org.apereo.cas;

import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;
import org.apereo.cas.config.AmazonSecretsManagerCloudConfigBootstrapConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.env.MockEnvironment;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.DeleteSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AmazonSecretsManagerCloudConfigBootstrapConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    AmazonSecretsManagerCloudConfigBootstrapConfiguration.class
}, properties = {
    "cas.spring.cloud.aws.secrets-manager.region=us-east-1",
    "cas.spring.cloud.aws.secrets-manager.endpoint=" + AmazonSecretsManagerCloudConfigBootstrapConfigurationTests.ENDPOINT,
    "cas.spring.cloud.aws.secrets-manager.credential-access-key=" + AmazonSecretsManagerCloudConfigBootstrapConfigurationTests.CREDENTIAL_ACCESS_KEY,
    "cas.spring.cloud.aws.secrets-manager.credential-secret-key=" + AmazonSecretsManagerCloudConfigBootstrapConfigurationTests.CREDENTIAL_SECRET_KEY
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnabledIfPortOpen(port = 4566)
@Tag("AmazonWebServices")
@Slf4j
public class AmazonSecretsManagerCloudConfigBootstrapConfigurationTests {

    static final String ENDPOINT = "http://localhost:4566";

    static final String CREDENTIAL_SECRET_KEY = "test";

    static final String CREDENTIAL_ACCESS_KEY = "test";

    private static final String STATIC_AUTHN_USERS = "casuser::WHATEVER";

    static {
        System.setProperty(SdkSystemSetting.AWS_ACCESS_KEY_ID.property(), "AKIAIPPIGGUNIO74C63Z");
        System.setProperty(SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property(), "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");
    }

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeAll
    public static void initialize() {
        val environment = new MockEnvironment();
        environment.setProperty(AmazonSecretsManagerCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "endpoint", ENDPOINT);
        environment.setProperty(AmazonSecretsManagerCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "region", Region.US_EAST_1.id());
        environment.setProperty(AmazonSecretsManagerCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credential-access-key", CREDENTIAL_ACCESS_KEY);
        environment.setProperty(AmazonSecretsManagerCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credential-secret-key", CREDENTIAL_SECRET_KEY);

        val builder = new AmazonEnvironmentAwareClientBuilder(AmazonSecretsManagerCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX, environment);
        val client = builder.build(SecretsManagerClient.builder(), SecretsManagerClient.class);

        try {
            client.deleteSecret(DeleteSecretRequest.builder()
                .secretId("cas.authn.accept.users")
                .forceDeleteWithoutRecovery(true).build());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage());
        }
        client.createSecret(CreateSecretRequest.builder().name("cas.authn.accept.users").secretString(STATIC_AUTHN_USERS).build());
        client.putSecretValue(PutSecretValueRequest.builder().secretId("cas.authn.accept.users").secretString(STATIC_AUTHN_USERS).build());
    }

    @Test
    public void verifyOperation() {
        assertEquals(STATIC_AUTHN_USERS, casProperties.getAuthn().getAccept().getUsers());
    }
}
