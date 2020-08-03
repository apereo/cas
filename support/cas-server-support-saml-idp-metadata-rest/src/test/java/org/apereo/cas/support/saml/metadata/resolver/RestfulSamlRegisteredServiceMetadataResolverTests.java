package org.apereo.cas.support.saml.metadata.resolver;

import org.apereo.cas.support.saml.BaseRestfulSamlMetadataTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.util.MockWebServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;

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
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private MockWebServer webServer;

    @BeforeEach
    @SneakyThrows
    public void initialize() {
        val doc = new SamlMetadataDocument();
        doc.setId(1);
        doc.setName("SAML Document");
        doc.setSignature(null);
        doc.setValue(IOUtils.toString(new ClassPathResource("sp-metadata.xml").getInputStream(), StandardCharsets.UTF_8));
        val data = MAPPER.writeValueAsString(doc);

        this.webServer = new MockWebServer(8078,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_XML_VALUE);

        this.webServer.start();
    }

    @AfterEach
    public void cleanup() {
        this.webServer.stop();
    }

    @Test
    public void verifyRestEndpointProducesMetadata() {
        val service = new SamlRegisteredService();
        service.setName("SAML Wiki Service");
        service.setServiceId("https://carmenwiki.osu.edu/shibboleth");
        service.setDescription("Testing");
        service.setMetadataLocation("rest://");
        assertTrue(resolver.supports(service));
        val resolvers = resolver.resolve(service);
        assertEquals(1, resolvers.size());
    }
}
