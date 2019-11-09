package org.apereo.cas.support.saml.idp.metadata.locator;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link FileSystemSamlIdPMetadataLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
public class FileSystemSamlIdPMetadataLocatorTests extends BaseSamlIdPConfigurationTests {
    @Test
    public void verifyOperation() {
        samlIdPMetadataLocator.initialize();
        assertNotNull(samlIdPMetadataGenerator.generate());
        assertNotNull(samlIdPMetadataLocator.resolveMetadata(Optional.empty()));
        assertNotNull(samlIdPMetadataLocator.getEncryptionCertificate(Optional.empty()));
        assertNotNull(samlIdPMetadataLocator.resolveEncryptionKey(Optional.empty()));
        assertNotNull(samlIdPMetadataLocator.resolveSigningCertificate(Optional.empty()));
        assertNotNull(samlIdPMetadataLocator.resolveSigningKey(Optional.empty()));
        assertTrue(samlIdPMetadataLocator.exists(Optional.empty()));
    }
}
