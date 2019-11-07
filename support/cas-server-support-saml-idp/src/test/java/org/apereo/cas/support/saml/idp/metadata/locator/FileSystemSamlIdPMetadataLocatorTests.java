package org.apereo.cas.support.saml.idp.metadata.locator;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

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
        assertNotNull(samlIdPMetadataLocator.resolveMetadata());
        assertNotNull(samlIdPMetadataLocator.getEncryptionCertificate());
        assertNotNull(samlIdPMetadataLocator.resolveEncryptionKey());
        assertNotNull(samlIdPMetadataLocator.resolveSigningCertificate());
        assertNotNull(samlIdPMetadataLocator.resolveSigningKey());
        assertTrue(samlIdPMetadataLocator.exists());
    }
}
