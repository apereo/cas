package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.BaseRestfulSamlMetadataTests;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.MockWebServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulSamlIdPMetadataLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("RestfulApi")
@TestPropertySource(properties = {
    "cas.authn.saml-idp.metadata.rest.url=http://localhost:9433",
    "cas.authn.saml-idp.metadata.rest.basicAuthUsername=user",
    "cas.authn.saml-idp.metadata.rest.basicAuthPassword=passw0rd",
    "cas.authn.saml-idp.metadata.rest.idp-metadata-enabled=true",
    "cas.authn.saml-idp.metadata.rest.crypto.enabled=false"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestfulSamlIdPMetadataLocatorTests extends BaseRestfulSamlMetadataTests {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private static MockWebServer SERVER;

    @Autowired
    @Qualifier("samlIdPMetadataLocator")
    protected SamlIdPMetadataLocator samlIdPMetadataLocator;

    @BeforeAll
    public static void setup() throws Exception {
        val document = new SamlIdPMetadataDocument(1000, "CAS",
            IOUtils.toString(new ClassPathResource("metadata/idp-metadata.xml").getInputStream(), StandardCharsets.UTF_8),
            IOUtils.toString(new ClassPathResource("metadata/idp-signing.crt").getInputStream(), StandardCharsets.UTF_8),
            IOUtils.toString(new ClassPathResource("metadata/idp-signing.key").getInputStream(), StandardCharsets.UTF_8),
            IOUtils.toString(new ClassPathResource("metadata/idp-encryption.crt").getInputStream(), StandardCharsets.UTF_8),
            IOUtils.toString(new ClassPathResource("metadata/idp-encryption.key").getInputStream(), StandardCharsets.UTF_8));
        val entity = MAPPER.writeValueAsString(document);
        val resource = new ByteArrayResource(entity.getBytes(StandardCharsets.UTF_8), "Output");
        SERVER = new MockWebServer(9433, resource, HttpStatus.OK);
        SERVER.start();
    }

    @AfterAll
    public static void tearDown() {
        SERVER.close();
    }
    
    @Test
    public void verifySigningKeyWithoutService() {
        assertNotNull(samlIdPMetadataLocator.resolveMetadata(Optional.empty()));

        val resource = samlIdPMetadataLocator.resolveSigningKey(Optional.empty());
        assertNotNull(resource);
    }
}
