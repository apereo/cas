package org.apereo.cas.heimdall;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.heimdall.authorizer.repository.AuthorizableResourceRepository;
import org.apereo.cas.heimdall.authorizer.resource.policy.OpenFGAAuthorizationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OpenFGAAuthorizationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("Authorization")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseHeimdallTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.oidc.jwks.file-system.jwks-file=file:${#systemProperties['java.io.tmpdir']}/heimdalloidc.jwks",
        "cas.heimdall.json.location=classpath:/policies"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
class OpenFGAAuthorizationPolicyTests {
    @Autowired
    @Qualifier(AuthorizableResourceRepository.BEAN_NAME)
    private AuthorizableResourceRepository authorizableResourceRepository;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void verifyOperation(final boolean allowed) throws Throwable {
        val mapper = JacksonObjectMapperFactory.builder().defaultTypingEnabled(false).build().toObjectMapper();
        val strategy = new OpenFGAAuthorizationPolicy();
        strategy.setRelation("reader");
        strategy.setStoreId("01GFTZWEZZMAM0NHQQZWE6AN3H");
        strategy.setToken(UUID.randomUUID().toString());
        val data = mapper.writeValueAsString(CollectionUtils.wrap("allowed", allowed));

        val authzRequest = AuthorizationRequest.builder()
            .uri("/api/claims")
            .method("PUT")
            .namespace("API_CLAIMS")
            .build()
            .withPrincipal(RegisteredServiceTestUtils.getPrincipal());

        val resource = authorizableResourceRepository.find(authzRequest).orElseThrow();
        try (val webServer = new MockWebServer(data)) {
            webServer.start();
            strategy.setApiUrl("http://localhost:%s".formatted(webServer.getPort()));
            assertEquals(allowed, strategy.evaluate(resource, authzRequest).authorized());
        }
    }

    @Test
    void verifyStatusCode4xx() {
        val strategy = new OpenFGAAuthorizationPolicy();
        strategy.setRelation("reader");
        strategy.setStoreId("01GFTZWEZZMAM0NHQQZWE6AN3H");
        strategy.setToken(UUID.randomUUID().toString());

        val authzRequest = AuthorizationRequest.builder()
            .uri("/api/claims")
            .method("PUT")
            .namespace("API_CLAIMS")
            .build()
            .withPrincipal(RegisteredServiceTestUtils.getPrincipal());

        val resource = authorizableResourceRepository.find(authzRequest).orElseThrow();
        try (val webServer = new MockWebServer(HttpStatus.FORBIDDEN)) {
            webServer.start();
            strategy.setApiUrl("http://localhost:%s".formatted(webServer.getPort()));
            assertFalse(strategy.evaluate(resource, authzRequest).authorized());
        }
    }
}
