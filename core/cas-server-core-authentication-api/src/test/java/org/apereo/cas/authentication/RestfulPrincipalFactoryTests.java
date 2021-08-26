package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulPrincipalFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RestfulApiAuthentication")
public class RestfulPrincipalFactoryTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Test
    public void verifyAction() throws Exception {
        val entity = MAPPER.writeValueAsString(CoreAuthenticationTestUtils.getPrincipal("casuser"));
        try (val webServer = new MockWebServer(9155,
            new ByteArrayResource(entity.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();

            val props = new RestEndpointProperties();
            props.setUrl("http://localhost:9155");
            val factory = PrincipalFactoryUtils.newRestfulPrincipalFactory(props);
            val p = factory.createPrincipal("casuser", CollectionUtils.wrap("name", List.of("CAS")));
            assertEquals("casuser", p.getId());
            assertEquals(5, p.getAttributes().size());
        }
    }

    @Test
    public void verifyNullPrincipal() throws Exception {
        val entity = MAPPER.writeValueAsString(CoreAuthenticationTestUtils.getPrincipal("casuser"));
        try (val webServer = new MockWebServer(9156,
            new ByteArrayResource(entity.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.EXPECTATION_FAILED)) {
            webServer.start();

            val props = new RestEndpointProperties();
            props.setUrl("http://localhost:9156");
            val factory = PrincipalFactoryUtils.newRestfulPrincipalFactory(props);
            val p = factory.createPrincipal("casuser", CollectionUtils.wrap("name", List.of("CAS")));
            assertNull(p);
        }
    }

    @Test
    public void verifyBadResponse() {
        try (val webServer = new MockWebServer(9157,
            new ByteArrayResource("abcde123456".getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();

            val props = new RestEndpointProperties();
            props.setUrl("http://localhost:9157");
            val factory = PrincipalFactoryUtils.newRestfulPrincipalFactory(props);
            assertThrows(IllegalArgumentException.class,
                () -> factory.createPrincipal("casuser", CollectionUtils.wrap("name", List.of("CAS"))));
        }
    }
}
