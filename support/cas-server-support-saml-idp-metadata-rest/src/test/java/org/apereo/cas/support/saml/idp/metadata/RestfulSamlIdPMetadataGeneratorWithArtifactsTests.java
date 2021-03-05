package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.BaseRestfulSamlMetadataTests;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
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
 * This is {@link RestfulSamlIdPMetadataGeneratorWithArtifactsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("RestfulApi")
@TestPropertySource(properties = {
    "cas.authn.saml-idp.metadata.rest.url=http://localhost:9443",
    "cas.authn.saml-idp.metadata.rest.basic-auth-username=user",
    "cas.authn.saml-idp.metadata.rest.basic-auth-password=passw0rd",
    "cas.authn.saml-idp.metadata.rest.idp-metadata-enabled=true",
    "cas.authn.saml-idp.metadata.rest.crypto.enabled=false"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestfulSamlIdPMetadataGeneratorWithArtifactsTests extends BaseRestfulSamlMetadataTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private static MockWebServer SERVER;

    @Autowired
    @Qualifier("samlIdPMetadataGenerator")
    protected SamlIdPMetadataGenerator samlIdPMetadataGenerator;

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
        SERVER = new MockWebServer(9443, resource, HttpStatus.OK);
        SERVER.start();
    }

    @AfterAll
    public static void tearDown() {
        SERVER.close();
    }

    @Test
    @Order(1)
    public void verifyOperation() {
        samlIdPMetadataGenerator.generate(Optional.empty());
        assertNotNull(samlIdPMetadataLocator.resolveMetadata(Optional.empty()));
        assertNotNull(samlIdPMetadataLocator.getEncryptionCertificate(Optional.empty()));
        assertNotNull(samlIdPMetadataLocator.resolveEncryptionKey(Optional.empty()));
        assertNotNull(samlIdPMetadataLocator.resolveSigningCertificate(Optional.empty()));
        assertNotNull(samlIdPMetadataLocator.resolveSigningKey(Optional.empty()));
    }

    @Test
    @Order(2)
    public void verifyService() {
        val service = new SamlRegisteredService();
        service.setName("TestShib");
        service.setId(1000);
        val registeredService = Optional.of(service);

        samlIdPMetadataGenerator.generate(registeredService);
        assertNotNull(samlIdPMetadataLocator.resolveMetadata(registeredService));
        assertNotNull(samlIdPMetadataLocator.getEncryptionCertificate(registeredService));
        assertNotNull(samlIdPMetadataLocator.resolveEncryptionKey(registeredService));
        assertNotNull(samlIdPMetadataLocator.resolveSigningCertificate(registeredService));
        assertNotNull(samlIdPMetadataLocator.resolveSigningKey(registeredService));
    }
}
