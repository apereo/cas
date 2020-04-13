package org.apereo.cas.support.saml.idp.metadata.generator;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link FileSystemSamlIdPMetadataGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
public class FileSystemSamlIdPMetadataGeneratorTests extends BaseSamlIdPConfigurationTests {

    @BeforeAll
    public static void beforeThisClass() {
        METADATA_DIRECTORY = new FileSystemResource(FileUtils.getTempDirectory());
    }

    @Test
    public void verifyOperation() throws Exception {
        assertNotNull(samlIdPMetadataGenerator.generate(Optional.empty()));
        assertNotNull(samlIdPMetadataLocator.resolveMetadata(Optional.empty()));
        assertNotNull(samlIdPMetadataLocator.getEncryptionCertificate(Optional.empty()));
        assertNotNull(samlIdPMetadataLocator.resolveEncryptionKey(Optional.empty()));
        assertNotNull(samlIdPMetadataLocator.resolveSigningCertificate(Optional.empty()));
        assertNotNull(samlIdPMetadataLocator.resolveSigningKey(Optional.empty()));
    }

    @Test
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
