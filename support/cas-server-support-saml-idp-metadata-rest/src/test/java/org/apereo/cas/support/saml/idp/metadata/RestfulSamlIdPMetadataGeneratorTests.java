package org.apereo.cas.support.saml.idp.metadata;

import org.apereo.cas.support.saml.BaseRestfulSamlMetadataTests;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apereo.cas.util.MockWebServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private static MockWebServer SERVER;

    @Autowired
    @Qualifier("samlIdPMetadataGenerator")
    protected SamlIdPMetadataGenerator samlIdPMetadataGenerator;

    @BeforeAll
    public static void setup() throws Exception {
        val entity = MAPPER.writeValueAsString(new SamlIdPMetadataDocument());
        val resource = new ByteArrayResource(entity.getBytes(StandardCharsets.UTF_8), "Output");
        SERVER = new MockWebServer(9453, resource, HttpStatus.OK);
        SERVER.start();
    }

    @AfterAll
    public static void tearDown() {
        SERVER.close();
    }

    @Test
    public void verifyOperation() {
        samlIdPMetadataGenerator.generate(Optional.empty());
        val service = new SamlRegisteredService();
        service.setName("TestShib");
        service.setId(1000);
        samlIdPMetadataGenerator.generate(Optional.of(service));
    }
}
