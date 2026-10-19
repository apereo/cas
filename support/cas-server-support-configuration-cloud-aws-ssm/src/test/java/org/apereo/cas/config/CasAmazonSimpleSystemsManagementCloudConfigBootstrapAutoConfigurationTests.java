package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.AmazonSimpleSystemsManagementPropertySource;
import org.apereo.cas.aws.AmazonEnvironmentAwareClientBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.api.MutablePropertySource;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterType;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @Autowired
    private ConfigurableEnvironment environment;
    
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

    @Test
    void verifyParametersAreLoadedOnce() {
        val client = mock(SsmClient.class);
        when(client.getParametersByPath(any(GetParametersByPathRequest.class)))
            .thenReturn(GetParametersByPathResponse.builder()
                .parameters(Parameter.builder().name("/cas/cas.server.name").value("https://apereo.org").build())
                .nextToken("page-2").build())
            .thenReturn(GetParametersByPathResponse.builder()
                .parameters(Parameter.builder().name("/cas/cas.server.prefix").value("https://apereo.org/cas").build())
                .build());
        val source = new AmazonSimpleSystemsManagementPropertySource("ssm", () -> client, List.of(StringUtils.EMPTY));
        assertEquals(Set.of("cas.server.name", "cas.server.prefix"), Set.of(source.getPropertyNames()));
        assertEquals("https://apereo.org", source.getProperty("cas.server.name"));
        assertEquals("https://apereo.org/cas", source.getProperty("cas.server.prefix"));
        verify(client, never()).getParameter(any(GetParameterRequest.class));
    }

    @Test
    void verifyEachOperationUsesAClientOfItsOwn() {
        val clients = new ArrayList<SsmClient>();
        val source = new AmazonSimpleSystemsManagementPropertySource("ssm", () -> {
            val client = mock(SsmClient.class);
            when(client.getParametersByPath(any(GetParametersByPathRequest.class)))
                .thenReturn(GetParametersByPathResponse.builder().build());
            clients.add(client);
            return client;
        }, List.of(StringUtils.EMPTY));
        source.setProperty("cas.server.name", "https://apereo.org");
        source.refresh();
        source.removeProperty("cas.server.name");
        assertEquals(4, clients.size());
        verify(clients.get(1)).putParameter(any(PutParameterRequest.class));
        clients.forEach(client -> verify(client).close());
    }
}
