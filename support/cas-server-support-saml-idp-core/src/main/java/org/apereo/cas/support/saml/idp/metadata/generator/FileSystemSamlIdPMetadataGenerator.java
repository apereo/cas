package org.apereo.cas.support.saml.idp.metadata.generator;

import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.writer.SamlIdPCertificateAndKeyWriter;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * A metadata generator based on a predefined template.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class FileSystemSamlIdPMetadataGenerator extends BaseSamlIdPMetadataGenerator {
    public FileSystemSamlIdPMetadataGenerator(final SamlIdPMetadataLocator samlIdPMetadataLocator,
                                              final SamlIdPCertificateAndKeyWriter samlIdPCertificateAndKeyWriter,
                                              final String entityId, final ResourceLoader resourceLoader,
                                              final String casServerPrefix, final String scope) {
        super(samlIdPMetadataLocator, samlIdPCertificateAndKeyWriter, entityId, resourceLoader, casServerPrefix, scope);
    }

    @Override
    @SneakyThrows
    public void buildSelfSignedEncryptionCert() {
        val encCert = this.samlIdPMetadataLocator.getEncryptionCertificate().getFile();
        val encKey = this.samlIdPMetadataLocator.getEncryptionKey().getFile();
        writeCertificateAndKey(encCert, encKey);
    }

    @Override
    @SneakyThrows
    public void buildSelfSignedSigningCert() {
        val signingCert = this.samlIdPMetadataLocator.getSigningCertificate().getFile();
        val signingKey = this.samlIdPMetadataLocator.getSigningKey().getFile();
        writeCertificateAndKey(signingCert, signingKey);
    }

    @Override
    @SneakyThrows
    protected void writeMetadata(final String metadata) {
        FileUtils.write(this.samlIdPMetadataLocator.getMetadata().getFile(), metadata, StandardCharsets.UTF_8);
    }

    @SneakyThrows
    private void writeCertificateAndKey(final File certificate, final File key) {
        if (certificate.exists()) {
            FileUtils.forceDelete(certificate);
        }
        if (key.exists()) {
            FileUtils.forceDelete(key);
        }
        try (val keyWriter = Files.newBufferedWriter(key.toPath(), StandardCharsets.UTF_8);
             val certWriter = Files.newBufferedWriter(certificate.toPath(), StandardCharsets.UTF_8)) {
            this.samlIdPCertificateAndKeyWriter.writeCertificateAndKey(keyWriter, certWriter);
        }
    }

    /**
     * Initializes a new Generate saml metadata.
     */
    @SneakyThrows
    public void initialize() {
        samlIdPMetadataLocator.initialize();
        generate();
    }
}
