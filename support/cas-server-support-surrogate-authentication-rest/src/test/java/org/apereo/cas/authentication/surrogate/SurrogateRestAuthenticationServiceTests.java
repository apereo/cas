package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.SurrogateRestAuthenticationConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
    SurrogateRestAuthenticationConfiguration.class,
    BaseSurrogateAuthenticationServiceTests.SharedTestConfiguration.class
},
    properties = "cas.authn.surrogate.rest.url=http://localhost:9301")
@Getter
public class SurrogateRestAuthenticationServiceTests extends BaseSurrogateAuthenticationServiceTests {
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .findAndRegisterModules()
        .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    @Qualifier("surrogateAuthenticationService")
    private SurrogateAuthenticationService service;

    private MockWebServer webServer;

    @Override
    @Test
    public void verifyList() {
        var data = StringUtils.EMPTY;
        try {
            data = MAPPER.writeValueAsString(CollectionUtils.wrapList("casuser", "otheruser"));
        } catch (final JsonProcessingException e) {
            throw new AssertionError(e);
        }
        try (val webServer = new MockWebServer(9301,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            this.webServer = webServer;
            this.webServer.start();
            assertTrue(this.webServer.isRunning());
            super.verifyList();
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    @Override
    @Test
    public void verifyProxying() {
        var data = StringUtils.EMPTY;
        try {
            data = MAPPER.writeValueAsString(CollectionUtils.wrapList("casuser", "otheruser"));
        } catch (final JsonProcessingException e) {
            throw new AssertionError(e);
        }
        try (val webServer = new MockWebServer(9310,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();

            val props = new CasConfigurationProperties();
            props.getAuthn().getSurrogate().getRest().setUrl("http://localhost:9310");
            val surrogateService = new SurrogateRestAuthenticationService(props.getAuthn().getSurrogate().getRest(), servicesManager);

            val result = surrogateService.canAuthenticateAs("cassurrogate",
                CoreAuthenticationTestUtils.getPrincipal("casuser"),
                Optional.of(CoreAuthenticationTestUtils.getService()));
            /*
             * Can't use super() until the REST classes are
             * completely refactored and don't need an actual server to connect to.
             */
            assertTrue(result);
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
