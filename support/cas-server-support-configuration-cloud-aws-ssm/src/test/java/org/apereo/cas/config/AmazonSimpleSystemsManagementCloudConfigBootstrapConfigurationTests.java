package org.apereo.cas.config;

import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

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
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;

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
    "cas.spring.cloud.aws.ssm.endpoint=" + AmazonSimpleSystemsManagementCloudConfigBootstrapConfigurationTests.ENDPOINT,
    "cas.spring.cloud.aws.ssm.region=us-east-1",
    "cas.spring.cloud.aws.ssm.credential-access-key=" + AmazonSimpleSystemsManagementCloudConfigBootstrapConfigurationTests.CREDENTIAL_ACCESS_KEY,
    "cas.spring.cloud.aws.ssm.credential-secret-key=" + AmazonSimpleSystemsManagementCloudConfigBootstrapConfigurationTests.CREDENTIAL_SECRET_KEY
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnabledIfPortOpen(port = 4566)
@Tag("AmazonWebServices")
@ActiveProfiles("example")
public class AmazonSimpleSystemsManagementCloudConfigBootstrapConfigurationTests {
    static final String ENDPOINT = "http://localhost:4566";

    static final String CREDENTIAL_SECRET_KEY = "test";

    static final String CREDENTIAL_ACCESS_KEY = "test";

    private static final String STATIC_AUTHN_USERS = "casuser::WHATEVER";

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeAll
    public static void initialize() {
        val environment = new MockEnvironment();

        environment.setProperty(AmazonSimpleSystemsManagementCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "endpoint", ENDPOINT);
        environment.setProperty(AmazonSimpleSystemsManagementCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "region", Region.US_EAST_1.id());
        environment.setProperty(AmazonSimpleSystemsManagementCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credential-access-key", CREDENTIAL_ACCESS_KEY);
        environment.setProperty(AmazonSimpleSystemsManagementCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credential-secret-key", CREDENTIAL_SECRET_KEY);

        val builder = new AmazonEnvironmentAwareClientBuilder(
            AmazonSimpleSystemsManagementCloudConfigBootstrapConfiguration.CAS_CONFIGURATION_PREFIX, environment);
        val client = builder.build(SsmClient.builder(), SsmClient.class);
        var request = PutParameterRequest.builder().name("/cas/cas.authn.accept.users").value(STATIC_AUTHN_USERS).overwrite(Boolean.TRUE).build();
        client.putParameter(request);

        request = PutParameterRequest.builder().name("/cas/example/cas.authn.accept.name").value("Example").overwrite(Boolean.TRUE).build();
        client.putParameter(request);
    }

    @Test
    public void verifyOperation() {
        assertEquals(STATIC_AUTHN_USERS, casProperties.getAuthn().getAccept().getUsers());
        assertEquals("Example", casProperties.getAuthn().getAccept().getName());
    }
}
