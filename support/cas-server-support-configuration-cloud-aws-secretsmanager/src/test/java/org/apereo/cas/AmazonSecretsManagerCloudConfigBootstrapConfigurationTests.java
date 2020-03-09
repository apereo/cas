package org.apereo.cas;

import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;
import org.apereo.cas.config.AmazonSecretsManagerCloudConfigBootstrapConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.PutSecretValueRequest;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.env.MockEnvironment;

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
    "cas.spring.cloud.aws.secretsManager.endpoint=" + AmazonSecretsManagerCloudConfigBootstrapConfigurationTests.ENDPOINT,
    "cas.spring.cloud.aws.secretsManager.credentialAccessKey=" + AmazonSecretsManagerCloudConfigBootstrapConfigurationTests.CREDENTIAL_ACCESS_KEY,
    "cas.spring.cloud.aws.secretsManager.credentialSecretKey=" + AmazonSecretsManagerCloudConfigBootstrapConfigurationTests.CREDENTIAL_SECRET_KEY
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnabledIfPortOpen(port = 4584)
@Tag("AmazonWebServices")
public class AmazonSecretsManagerCloudConfigBootstrapConfigurationTests {

    static final String ENDPOINT = "http://127.0.0.1:4584";
    static final String CREDENTIAL_SECRET_KEY = "test";
    static final String CREDENTIAL_ACCESS_KEY = "test";

    private static final String STATIC_AUTHN_USERS = "casuser::WHATEVER";

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeAll
    public static void initialize() {
        val environment = new MockEnvironment();
        environment.setProperty(AmazonSecretsManagerCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "endpoint", ENDPOINT);
        environment.setProperty(AmazonSecretsManagerCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credentialAccessKey", CREDENTIAL_ACCESS_KEY);
        environment.setProperty(AmazonSecretsManagerCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credentialSecretKey", CREDENTIAL_SECRET_KEY);

        val builder = new AmazonEnvironmentAwareClientBuilder(AmazonSecretsManagerCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX, environment);
        val client = builder.build(AWSSecretsManagerClientBuilder.standard(), AWSSecretsManager.class);

        val request = new PutSecretValueRequest();
        request.setSecretId("cas.authn.accept.users");
        request.setSecretString(STATIC_AUTHN_USERS);
        client.putSecretValue(request);
    }

    @Test
    public void verifyOperation() {
        assertEquals(STATIC_AUTHN_USERS, casProperties.getAuthn().getAccept().getUsers());
    }
}
