package org.apereo.cas.support.saml.idp.metadata.generator;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.writer.SamlIdPCertificateAndKeyWriter;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
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
        super(samlIdPMetadataLocator, samlIdPCertificateAndKeyWriter, CipherExecutor.noOpOfStringToString(),
            entityId, resourceLoader, casServerPrefix, scope);
    }

    @Override
    @SneakyThrows
    public Pair<String, String> buildSelfSignedEncryptionCert() {
        val encCert = this.samlIdPMetadataLocator.getEncryptionCertificate().getFile();
        val encKey = this.samlIdPMetadataLocator.getEncryptionKey().getFile();
        writeCertificateAndKey(encCert, encKey);
        return Pair.of(FileUtils.readFileToString(encCert, StandardCharsets.UTF_8), FileUtils.readFileToString(encKey, StandardCharsets.UTF_8));
    }

    @Override
    @SneakyThrows
    public Pair<String, String> buildSelfSignedSigningCert() {
        val signingCert = this.samlIdPMetadataLocator.getSigningCertificate().getFile();
        val signingKey = this.samlIdPMetadataLocator.getSigningKey().getFile();
        writeCertificateAndKey(signingCert, signingKey);
        return Pair.of(FileUtils.readFileToString(signingCert, StandardCharsets.UTF_8), FileUtils.readFileToString(signingKey, StandardCharsets.UTF_8));
    }

    @Override
    @SneakyThrows
    protected String writeMetadata(final String metadata) {
        FileUtils.write(this.samlIdPMetadataLocator.getMetadata().getFile(), metadata, StandardCharsets.UTF_8);
        return metadata;
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
