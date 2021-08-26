package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.BaseRestfulSamlMetadataTests;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulSamlIdPMetadataGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("RestfulApi")
@TestPropertySource(properties = {
    "cas.authn.saml-idp.metadata.rest.url=http://localhost:9453",
    "cas.authn.saml-idp.metadata.rest.basic-auth-username=user",
    "cas.authn.saml-idp.metadata.rest.basic-auth-password=passw0rd",
    "cas.authn.saml-idp.metadata.rest.idp-metadata-enabled=true",
    "cas.authn.saml-idp.metadata.rest.crypto.enabled=true"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestfulSamlIdPMetadataGeneratorTests extends BaseRestfulSamlMetadataTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    @Qualifier("samlIdPMetadataGenerator")
    protected SamlIdPMetadataGenerator samlIdPMetadataGenerator;

    @Test
    public void verifyOperation() throws Exception {
        var document = new SamlIdPMetadataDocument();
        var entity = MAPPER.writeValueAsString(document);
        try (val webServer = new MockWebServer(9453,
            new ByteArrayResource(entity.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            assertNotNull(samlIdPMetadataGenerator.generate(Optional.empty()));
        }

        document.setEncryptionCertificate(UUID.randomUUID().toString());
        document.setSigningKey(UUID.randomUUID().toString());
        document.setSigningCertificate(UUID.randomUUID().toString());
        document.setEncryptionKey(UUID.randomUUID().toString());
        document.setMetadata(UUID.randomUUID().toString());

        entity = MAPPER.writeValueAsString(document);
        try (val webServer = new MockWebServer(9453,
            new ByteArrayResource(entity.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            val service = new SamlRegisteredService();
            service.setName("TestShib");
            service.setId(1000);
            assertNotNull(samlIdPMetadataGenerator.generate(Optional.of(service)));
        }

        try (val webServer = new MockWebServer(9453,
            new ByteArrayResource("___".getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            assertNotNull(samlIdPMetadataGenerator.generate(Optional.empty()));
        }
    }
}
