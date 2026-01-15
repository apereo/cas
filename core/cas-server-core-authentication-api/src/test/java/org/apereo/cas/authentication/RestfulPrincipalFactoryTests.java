package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulPrincipalFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RestfulApiAuthentication")
class RestfulPrincipalFactoryTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Test
    void verifyAction() throws Throwable {
        val entity = MAPPER.writeValueAsString(CoreAuthenticationTestUtils.getPrincipal("casuser"));
        try (val webServer = new MockWebServer(entity)) {
            webServer.start();

            val props = new RestEndpointProperties();
            props.setUrl("http://localhost:%s".formatted(webServer.getPort()));
            val factory = PrincipalFactoryUtils.newRestfulPrincipalFactory(props);
            val p = factory.createPrincipal("casuser", CollectionUtils.wrap("name", List.of("CAS")));
            assertEquals("casuser", p.getId());
            assertEquals(5, p.getAttributes().size());
        }
    }

    @Test
    void verifyNullPrincipal() throws Throwable {
        val entity = MAPPER.writeValueAsString(CoreAuthenticationTestUtils.getPrincipal("casuser"));
        try (val webServer = new MockWebServer(entity, HttpStatus.EXPECTATION_FAILED)) {
            webServer.start();

            val props = new RestEndpointProperties();
            props.setUrl("http://localhost:%s".formatted(webServer.getPort()));
            val factory = PrincipalFactoryUtils.newRestfulPrincipalFactory(props);
            val p = factory.createPrincipal("casuser", CollectionUtils.wrap("name", List.of("CAS")));
            assertNull(p);
        }
    }

    @Test
    void verifyBadResponse() {
        try (val webServer = new MockWebServer("abcde123456")) {
            webServer.start();
            val props = new RestEndpointProperties();
            props.setUrl("http://localhost:%s".formatted(webServer.getPort()));
            val factory = PrincipalFactoryUtils.newRestfulPrincipalFactory(props);
            assertThrows(IllegalArgumentException.class,
                () -> factory.createPrincipal("casuser", CollectionUtils.wrap("name", List.of("CAS"))));
        }
    }
}
