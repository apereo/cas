package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;

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
@Tag("RestfulApi")
public class RestfulPrincipalFactoryTests {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verifyAction() throws Exception {
        val entity = MAPPER.writeValueAsString(CoreAuthenticationTestUtils.getPrincipal("casuser"));
        try (val webServer = new MockWebServer(9155,
            new ByteArrayResource(entity.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();

            val factory = PrincipalFactoryUtils.newRestfulPrincipalFactory("http://localhost:9155", null, null);
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

            val factory = PrincipalFactoryUtils.newRestfulPrincipalFactory("http://localhost:9156", null, null);
            val p = factory.createPrincipal("casuser", CollectionUtils.wrap("name", List.of("CAS")));
            assertNull(p);
        }
    }

    @Test
    public void verifyBadResponse() throws Exception {
        try (val webServer = new MockWebServer(9157,
            new ByteArrayResource("abcde123456".getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();

            val factory = PrincipalFactoryUtils.newRestfulPrincipalFactory("http://localhost:9157", null, null);
            assertThrows(IllegalArgumentException.class,
                () -> factory.createPrincipal("casuser", CollectionUtils.wrap("name", List.of("CAS"))));
        }
    }
}
