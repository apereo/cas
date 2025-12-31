package org.apereo.cas;

import module java.base;
import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;
import org.apereo.cas.config.CasAmazonSecretsManagerCloudConfigBootstrapAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.api.MutablePropertySource;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.bootstrap.config.BootstrapPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.CreateSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.DeleteSecretRequest;
import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasAmazonSecretsManagerCloudConfigBootstrapAutoConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasAmazonSecretsManagerCloudConfigBootstrapAutoConfiguration.class, properties = {
    "cas.spring.cloud.aws.secrets-manager.region=us-east-1",
    "cas.spring.cloud.aws.secrets-manager.endpoint=" + CasAmazonSecretsManagerCloudConfigBootstrapAutoConfigurationTests.ENDPOINT,
    "cas.spring.cloud.aws.secrets-manager.credential-access-key=" + CasAmazonSecretsManagerCloudConfigBootstrapAutoConfigurationTests.CREDENTIAL_ACCESS_KEY,
    "cas.spring.cloud.aws.secrets-manager.credential-secret-key=" + CasAmazonSecretsManagerCloudConfigBootstrapAutoConfigurationTests.CREDENTIAL_SECRET_KEY
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnabledIfListeningOnPort(port = 4566)
@Tag("AmazonWebServices")
@ExtendWith(CasTestExtension.class)
@Slf4j
class CasAmazonSecretsManagerCloudConfigBootstrapAutoConfigurationTests {

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

    @Autowired
    private ConfigurableEnvironment environment;

    @BeforeAll
    public static void initialize() {
        val environment = new MockEnvironment();
        environment.setProperty(CasAmazonSecretsManagerCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "endpoint", ENDPOINT);
        environment.setProperty(CasAmazonSecretsManagerCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "region", Region.US_EAST_1.id());
        environment.setProperty(CasAmazonSecretsManagerCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credential-access-key", CREDENTIAL_ACCESS_KEY);
        environment.setProperty(CasAmazonSecretsManagerCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credential-secret-key", CREDENTIAL_SECRET_KEY);

        val builder = new AmazonEnvironmentAwareClientBuilder(CasAmazonSecretsManagerCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX, environment);
        try (val client = builder.build(SecretsManagerClient.builder(), SecretsManagerClient.class)) {
            client.deleteSecret(DeleteSecretRequest.builder()
                .secretId("cas.authn.accept.users")
                .forceDeleteWithoutRecovery(true).build());
            client.createSecret(CreateSecretRequest.builder().name("cas.authn.accept.users").secretString(STATIC_AUTHN_USERS).build());
            client.putSecretValue(PutSecretValueRequest.builder().secretId("cas.authn.accept.users").secretString(STATIC_AUTHN_USERS).build());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Test
    void verifyOperation() {
        assertEquals(STATIC_AUTHN_USERS, casProperties.getAuthn().getAccept().getUsers());

        val propertySource = environment.getPropertySources()
            .stream()
            .filter(source -> source instanceof BootstrapPropertySource<?>)
            .map(BootstrapPropertySource.class::cast)
            .map(BootstrapPropertySource::getDelegate)
            .filter(MutablePropertySource.class::isInstance)
            .map(MutablePropertySource.class::cast)
            .findFirst()
            .orElseThrow();
        propertySource.setProperty("cas.server.prefix", "https://example.org/cas");
        propertySource.setProperty("cas.server.prefix", "https://apereo.org/cas");
        val prefix = environment.getProperty("cas.server.prefix");
        assertEquals("https://apereo.org/cas", prefix);
        propertySource.removeProperty("cas.server.prefix");
        assertNull(environment.getProperty("cas.server.prefix"));
        propertySource.removeAll();
        assertEquals(0, propertySource.getPropertyNames().length);
    }
}
