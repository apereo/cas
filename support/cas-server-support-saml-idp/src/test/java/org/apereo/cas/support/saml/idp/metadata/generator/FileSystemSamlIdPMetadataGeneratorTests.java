package org.apereo.cas.support.saml.idp.metadata.generator;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link FileSystemSamlIdPMetadataGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
public class FileSystemSamlIdPMetadataGeneratorTests extends BaseSamlIdPConfigurationTests {
    @Test
    public void verifyOperation() {
        assertNotNull(samlIdPMetadataGenerator.generate());
        assertNotNull(samlIdPMetadataLocator.resolveMetadata());
        assertNotNull(samlIdPMetadataLocator.getEncryptionCertificate());
        assertNotNull(samlIdPMetadataLocator.resolveEncryptionKey());
        assertNotNull(samlIdPMetadataLocator.resolveSigningCertificate());
        assertNotNull(samlIdPMetadataLocator.resolveSigningKey());
    }
}
