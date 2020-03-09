package org.apereo.cas.config;

import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.PutParameterRequest;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AmazonSimpleSystemsManagementCloudConfigBootstrapConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    AmazonSimpleSystemsManagementCloudConfigBootstrapConfiguration.class
}, properties = {
    "cas.spring.cloud.aws.ssm.endpoint="
        + AmazonSimpleSystemsManagementCloudConfigBootstrapConfigurationTests.ENDPOINT,
    "cas.spring.cloud.aws.ssm.credentialAccessKey="
        + AmazonSimpleSystemsManagementCloudConfigBootstrapConfigurationTests.CREDENTIAL_ACCESS_KEY,
    "cas.spring.cloud.aws.ssm.credentialSecretKey="
        + AmazonSimpleSystemsManagementCloudConfigBootstrapConfigurationTests.CREDENTIAL_SECRET_KEY
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnabledIfPortOpen(port = 4583)
@Tag("AmazonWebServices")
@ActiveProfiles("example")
public class AmazonSimpleSystemsManagementCloudConfigBootstrapConfigurationTests {
    static final String ENDPOINT = "http://127.0.0.1:4583";

    static final String CREDENTIAL_SECRET_KEY = "test";

    static final String CREDENTIAL_ACCESS_KEY = "test";

    private static final String STATIC_AUTHN_USERS = "casuser::WHATEVER";

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeAll
    public static void initialize() {
        val environment = new MockEnvironment();

        environment.setProperty(AmazonSimpleSystemsManagementCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX
            + '.' + "endpoint", ENDPOINT);
        environment.setProperty(AmazonSimpleSystemsManagementCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX
            + '.' + "credentialAccessKey", CREDENTIAL_ACCESS_KEY);
        environment.setProperty(AmazonSimpleSystemsManagementCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX
            + '.' + "credentialSecretKey", CREDENTIAL_SECRET_KEY);

        val builder = new AmazonEnvironmentAwareClientBuilder(
            AmazonSimpleSystemsManagementCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX, environment);
        val client = builder.build(AWSSimpleSystemsManagementClientBuilder.standard(), AWSSimpleSystemsManagement.class);

        var request = new PutParameterRequest();
        request.setName("/cas/cas.authn.accept.users");
        request.setValue(STATIC_AUTHN_USERS);
        request.setOverwrite(Boolean.TRUE);
        client.putParameter(request);

        request.setName("/cas/example/cas.authn.accept.name");
        request.setValue("Example");
        request.setOverwrite(Boolean.TRUE);
        client.putParameter(request);
    }

    @Test
    public void verifyOperation() {
        assertEquals(STATIC_AUTHN_USERS, casProperties.getAuthn().getAccept().getUsers());
        assertEquals("Example", casProperties.getAuthn().getAccept().getName());
    }
}
