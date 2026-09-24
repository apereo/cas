package org.apereo.cas.authentication.surrogate;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasSurrogateRestAuthenticationAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServicePrincipalAccessStrategyEnforcer;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SurrogateRestAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RestfulApi")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = {
    CasSurrogateRestAuthenticationAutoConfiguration.class,
    BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class
},
    properties = "cas.authn.surrogate.rest.url=https://localhost/surrogates")
class SurrogateRestAuthenticationServiceTests extends BaseSurrogateAuthenticationServiceTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder().build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(SurrogateAuthenticationService.BEAN_NAME)
    private SurrogateAuthenticationService service;

    @Autowired
    @Qualifier(RegisteredServicePrincipalAccessStrategyEnforcer.BEAN_NAME)
    private RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer;

    private SurrogateAuthenticationService serviceUnderTest;

    @Override
    public SurrogateAuthenticationService getService() {
        return serviceUnderTest != null ? serviceUnderTest : service;
    }
    
    @Override
    @Test
    void verifyUserAllowedToProxy() throws Throwable {
        val data = MAPPER.writeValueAsString(CollectionUtils.wrapList("casuser", "otheruser"));
        try (val webServer = new MockWebServer(
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertTrue(webServer.isRunning());
            this.serviceUnderTest = restServiceFor(webServer);
            assertUserIsAllowedToProxy();
        }
    }

    @Override
    @Test
    void verifyUserNotAllowedToProxy() throws Throwable {
        val data = MAPPER.writeValueAsString(CollectionUtils.wrapList());
        try (val webServer = new MockWebServer(
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertTrue(webServer.isRunning());
            this.serviceUnderTest = restServiceFor(webServer);
            assertUserIsNotAllowedToProxy();
        }
    }

    @Override
    @Test
    void verifyWildcard() throws Throwable {
        val data = MAPPER.writeValueAsString(CollectionUtils.wrapList(SurrogateAuthenticationService.WILDCARD_ACCOUNT));
        try (val webServer = new MockWebServer(
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertTrue(webServer.isRunning());
            this.serviceUnderTest = restServiceFor(webServer);
            assertWildcardImpersonation();
        }
    }

    @Override
    @Test
    void verifyProxying() throws Throwable {
        var data = MAPPER.writeValueAsString(CollectionUtils.wrapList("casuser", "otheruser"));
        try (val webServer = new MockWebServer(data)) {
            webServer.start();

            val props = new CasConfigurationProperties();
            props.getAuthn().getSurrogate().getRest().setUrl("http://localhost:%s".formatted(webServer.getPort()));
            val surrogateService = new SurrogateRestAuthenticationService(servicesManager, props, principalAccessStrategyEnforcer, applicationContext);

            val application = CoreAuthenticationTestUtils.getService(UUID.randomUUID().toString());
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(application.getId(), Map.of());
            servicesManager.save(registeredService);
            
            val result = surrogateService.canImpersonate("cassurrogate",
                CoreAuthenticationTestUtils.getPrincipal("casuser"),
                Optional.of(application));
            /*
             * Can't use super() until the REST classes are
             * completely refactored and don't need an actual server to connect to.
             */
            assertTrue(result);
        }
    }

    private SurrogateRestAuthenticationService restServiceFor(final MockWebServer webServer) {
        val props = new CasConfigurationProperties();
        props.getAuthn().getSurrogate().getRest().setUrl("http://localhost:%s".formatted(webServer.getPort()));
        return new SurrogateRestAuthenticationService(servicesManager, props, principalAccessStrategyEnforcer, applicationContext);
    }

    @Test
    void verifyBadResponse() {
        var data = MAPPER.writeValueAsString("@@@");
        try (val webServer = new MockWebServer(data)) {
            webServer.start();
            val props = new CasConfigurationProperties();
            props.getAuthn().getSurrogate().getRest().setUrl("http://localhost:%s".formatted(webServer.getPort()));
            val surrogateService = new SurrogateRestAuthenticationService(servicesManager, props, principalAccessStrategyEnforcer, applicationContext);
            val result = surrogateService.getImpersonationAccounts("cassurrogate", Optional.empty());
            assertTrue(result.isEmpty());
        }
    }
}
