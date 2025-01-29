package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasSurrogateRestAuthenticationAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServicePrincipalAccessStrategyEnforcer;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
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
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
    properties = "cas.authn.surrogate.rest.url=http://localhost:${random.int[3000,9000]}")
@Getter
class SurrogateRestAuthenticationServiceTests extends BaseSurrogateAuthenticationServiceTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder().build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Autowired
    @Qualifier(SurrogateAuthenticationService.BEAN_NAME)
    private SurrogateAuthenticationService service;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(RegisteredServicePrincipalAccessStrategyEnforcer.BEAN_NAME)
    private RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer;
    
    @Override
    @Test
    void verifyUserAllowedToProxy() throws Throwable {
        val props = casProperties.getAuthn().getSurrogate().getRest();
        val port = URI.create(props.getUrl()).getPort();
        var data = MAPPER.writeValueAsString(CollectionUtils.wrapList("casuser", "otheruser"));
        try (val webServer = new MockWebServer(port,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertTrue(webServer.isRunning());
            super.verifyUserAllowedToProxy();
        }
    }

    @Override
    @Test
    void verifyUserNotAllowedToProxy() throws Throwable {
        val props = casProperties.getAuthn().getSurrogate().getRest();
        val port = URI.create(props.getUrl()).getPort();
        var data = MAPPER.writeValueAsString(CollectionUtils.wrapList());
        try (val webServer = new MockWebServer(port,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertTrue(webServer.isRunning());
            super.verifyUserNotAllowedToProxy();
        }
    }

    @Override
    @Test
    void verifyWildcard() throws Throwable {
        val props = casProperties.getAuthn().getSurrogate().getRest();
        val port = URI.create(props.getUrl()).getPort();
        var data = MAPPER.writeValueAsString(CollectionUtils.wrapList(SurrogateAuthenticationService.WILDCARD_ACCOUNT));
        try (val webServer = new MockWebServer(port,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertTrue(webServer.isRunning());
            super.verifyWildcard();
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

    @Test
    void verifyBadResponse() throws Throwable {
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
