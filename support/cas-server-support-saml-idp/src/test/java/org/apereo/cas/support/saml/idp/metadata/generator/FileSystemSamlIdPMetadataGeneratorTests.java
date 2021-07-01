package org.apereo.cas.support.saml.idp.metadata.generator;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link FileSystemSamlIdPMetadataGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLMetadata")
@TestPropertySource(properties = {
    "cas.authn.saml-idp.core.entity-id=https://cas.example.org/idp",
    "cas.authn.saml-idp.core.cache-expiration=0",
    "cas.authn.saml-idp.metadata.file-system.sign-metadata=true",
    "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata"
})
public class FileSystemSamlIdPMetadataGeneratorTests extends BaseSamlIdPConfigurationTests {

    @BeforeEach
    public void setup() throws Exception {
        val f = new File(FileUtils.getTempDirectory(), "idp-metadata");
        FileUtils.deleteDirectory(f);
        assertFalse(f.exists());
        assertTrue(f.mkdirs());
    }

    @Test
    public void verifyOperation() throws Exception {
        assertNotNull(samlIdPMetadataGenerator.generate(Optional.empty()));
        val metadata = samlIdPMetadataLocator.resolveMetadata(Optional.empty());
        assertNotNull(metadata);
        assertNotNull(samlIdPMetadataLocator.getEncryptionCertificate(Optional.empty()));
        assertNotNull(samlIdPMetadataLocator.resolveEncryptionKey(Optional.empty()));
        assertNotNull(samlIdPMetadataLocator.resolveSigningCertificate(Optional.empty()));
        assertNotNull(samlIdPMetadataLocator.resolveSigningKey(Optional.empty()));

        FileUtils.delete(metadata.getFile());
        assertNotNull(samlIdPMetadataGenerator.generate(Optional.empty()));
    }

    @Test
    public void verifyService() {
        assertNotNull(samlIdPMetadataGenerator.generate(Optional.empty()));

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
