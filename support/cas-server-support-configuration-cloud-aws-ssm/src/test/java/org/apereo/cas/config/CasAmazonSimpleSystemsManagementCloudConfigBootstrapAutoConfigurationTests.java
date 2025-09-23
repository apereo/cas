package org.apereo.cas.config;

import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.ParameterType;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasAmazonSimpleSystemsManagementCloudConfigBootstrapAutoConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasAmazonSimpleSystemsManagementCloudConfigBootstrapAutoConfiguration.class, properties = {
    "cas.spring.cloud.aws.ssm.endpoint=" + CasAmazonSimpleSystemsManagementCloudConfigBootstrapAutoConfigurationTests.ENDPOINT,
    "cas.spring.cloud.aws.ssm.region=us-east-1",
    "cas.spring.cloud.aws.ssm.credential-access-key=" + CasAmazonSimpleSystemsManagementCloudConfigBootstrapAutoConfigurationTests.CREDENTIAL_ACCESS_KEY,
    "cas.spring.cloud.aws.ssm.credential-secret-key=" + CasAmazonSimpleSystemsManagementCloudConfigBootstrapAutoConfigurationTests.CREDENTIAL_SECRET_KEY
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnabledIfListeningOnPort(port = 4566)
@Tag("AmazonWebServices")
@ExtendWith(CasTestExtension.class)
@ActiveProfiles("example")
class CasAmazonSimpleSystemsManagementCloudConfigBootstrapAutoConfigurationTests {
    static final String ENDPOINT = "http://localhost:4566";

    static final String CREDENTIAL_SECRET_KEY = "test";

    static final String CREDENTIAL_ACCESS_KEY = "test";

    private static final String STATIC_AUTHN_USERS = "casuser::WHATEVER";

    @Autowired
    private CasConfigurationProperties casProperties;

    @BeforeAll
    public static void initialize() {
        val environment = new MockEnvironment();

        environment.setProperty(CasAmazonSimpleSystemsManagementCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "endpoint", ENDPOINT);
        environment.setProperty(CasAmazonSimpleSystemsManagementCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "region", Region.US_EAST_1.id());
        environment.setProperty(CasAmazonSimpleSystemsManagementCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credential-access-key", CREDENTIAL_ACCESS_KEY);
        environment.setProperty(CasAmazonSimpleSystemsManagementCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX + '.' + "credential-secret-key", CREDENTIAL_SECRET_KEY);

        val builder = new AmazonEnvironmentAwareClientBuilder(
            CasAmazonSimpleSystemsManagementCloudConfigBootstrapAutoConfiguration.CAS_CONFIGURATION_PREFIX, environment);
        try (val client = builder.build(SsmClient.builder(), SsmClient.class)) {
            var request = PutParameterRequest.builder().name("/cas/cas.authn.accept.users")
                .type(ParameterType.STRING)
                .value(STATIC_AUTHN_USERS)
                .overwrite(Boolean.TRUE)
                .build();
            client.putParameter(request);

            request = PutParameterRequest.builder().name("/cas/example/cas.authn.accept.name")
                .type(ParameterType.STRING)
                .value("Example")
                .overwrite(Boolean.TRUE)
                .build();
            client.putParameter(request);
        }
    }

    @Test
    void verifyOperation() {
        assertEquals(STATIC_AUTHN_USERS, casProperties.getAuthn().getAccept().getUsers());
        assertEquals("Example", casProperties.getAuthn().getAccept().getName());
    }
}
