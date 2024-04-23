package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasSurrogateRestAuthenticationAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SurrogateRestAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RestfulApi")
@SpringBootTest(classes = {
    CasSurrogateRestAuthenticationAutoConfiguration.class,
    BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class
},
    properties = "cas.authn.surrogate.rest.url=http://localhost:9301")
@Getter
class SurrogateRestAuthenticationServiceTests extends BaseSurrogateAuthenticationServiceTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder().build().toObjectMapper();

    @Autowired
    @Qualifier(SurrogateAuthenticationService.BEAN_NAME)
    private SurrogateAuthenticationService service;

    @Override
    @Test
    void verifyUserAllowedToProxy() throws Throwable {
        var data = MAPPER.writeValueAsString(CollectionUtils.wrapList("casuser", "otheruser"));
        try (val webServer = new MockWebServer(9301,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertTrue(webServer.isRunning());
            super.verifyUserAllowedToProxy();
        }
    }

    @Override
    @Test
    void verifyUserNotAllowedToProxy() throws Throwable {
        var data = MAPPER.writeValueAsString(CollectionUtils.wrapList());
        try (val webServer = new MockWebServer(9301,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertTrue(webServer.isRunning());
            super.verifyUserNotAllowedToProxy();
        }
    }

    @Override
    @Test
    void verifyWildcard() throws Throwable {
        var data = MAPPER.writeValueAsString(CollectionUtils.wrapList(SurrogateAuthenticationService.WILDCARD_ACCOUNT));
        try (val webServer = new MockWebServer(9301,
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
            val surrogateService = new SurrogateRestAuthenticationService(props.getAuthn().getSurrogate().getRest(), servicesManager);

            val result = surrogateService.canImpersonate("cassurrogate",
                CoreAuthenticationTestUtils.getPrincipal("casuser"),
                Optional.of(CoreAuthenticationTestUtils.getService()));
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
            val surrogateService = new SurrogateRestAuthenticationService(props.getAuthn().getSurrogate().getRest(), servicesManager);
            val result = surrogateService.getImpersonationAccounts("cassurrogate", Optional.empty());
            assertTrue(result.isEmpty());
        }
    }
}
