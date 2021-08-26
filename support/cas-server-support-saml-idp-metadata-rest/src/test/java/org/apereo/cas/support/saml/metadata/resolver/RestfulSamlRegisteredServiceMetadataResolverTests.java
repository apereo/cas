package org.apereo.cas.support.saml.metadata.resolver;

import org.apereo.cas.support.saml.BaseRestfulSamlMetadataTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulSamlRegisteredServiceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RestfulApi")
public class RestfulSamlRegisteredServiceMetadataResolverTests extends BaseRestfulSamlMetadataTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Test
    public void verifyRestEndpointProducesMetadata() throws Exception {
        val service = new SamlRegisteredService();
        service.setName("SAML Wiki Service");
        service.setServiceId("https://carmenwiki.osu.edu/shibboleth");
        service.setDescription("Testing");
        service.setMetadataLocation("rest://");
        assertTrue(resolver.supports(service));
        assertFalse(resolver.supports(null));
        assertFalse(resolver.isAvailable(service));
        assertFalse(resolver.isAvailable(null));

        val doc = new SamlMetadataDocument();
        doc.setId(1);
        doc.setName("SAML Document");
        doc.setSignature(null);
        doc.setValue(IOUtils.toString(new ClassPathResource("sp-metadata.xml").getInputStream(), StandardCharsets.UTF_8));
        val entity = MAPPER.writeValueAsString(doc);

        try (val webServer = new MockWebServer(8078,
            new ByteArrayResource(entity.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            assertTrue(resolver.isAvailable(service));
            val resolvers = resolver.resolve(service);
            assertEquals(1, resolvers.size());
        }

        try (val webServer = new MockWebServer(8078,
            new ByteArrayResource("@$@".getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            val resolvers = resolver.resolve(service);
            assertEquals(0, resolvers.size());
        }
    }
}
