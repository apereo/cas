package org.apereo.cas.support.saml.idp.metadata.generator;

import org.apereo.cas.support.saml.services.SamlRegisteredService;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

/**
 * A metadata generator based on a predefined template.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class FileSystemSamlIdPMetadataGenerator extends BaseSamlIdPMetadataGenerator implements InitializingBean {
    public FileSystemSamlIdPMetadataGenerator(final SamlIdPMetadataGeneratorConfigurationContext context) {
        super(context);
    }

    @Override
    @SneakyThrows
    public Pair<String, String> buildSelfSignedEncryptionCert(final Optional<SamlRegisteredService> registeredService) {
        val encCert = getConfigurationContext().getSamlIdPMetadataLocator()
            .getEncryptionCertificate(registeredService).getFile();
        val encKey = getConfigurationContext().getSamlIdPMetadataLocator()
            .resolveEncryptionKey(registeredService).getFile();
        writeCertificateAndKey(encCert, encKey);
        return Pair.of(FileUtils.readFileToString(encCert, StandardCharsets.UTF_8),
            FileUtils.readFileToString(encKey, StandardCharsets.UTF_8));
    }

    @Override
    @SneakyThrows
    public Pair<String, String> buildSelfSignedSigningCert(final Optional<SamlRegisteredService> registeredService) {
        val signingCert = getConfigurationContext().getSamlIdPMetadataLocator()
            .resolveSigningCertificate(registeredService).getFile();
        val signingKey = getConfigurationContext().getSamlIdPMetadataLocator()
            .resolveSigningKey(registeredService).getFile();
        writeCertificateAndKey(signingCert, signingKey);
        return Pair.of(FileUtils.readFileToString(signingCert, StandardCharsets.UTF_8),
            FileUtils.readFileToString(signingKey, StandardCharsets.UTF_8));
    }

    @Override
    @SneakyThrows
    protected String writeMetadata(final String metadata, final Optional<SamlRegisteredService> registeredService) {
        FileUtils.write(getConfigurationContext().getSamlIdPMetadataLocator()
            .resolveMetadata(registeredService).getFile(), metadata, StandardCharsets.UTF_8);
        return metadata;
    }

    @SneakyThrows
    protected void writeCertificateAndKey(final File certificate, final File key) {
        if (certificate.exists()) {
            LOGGER.info("Certificate file [{}] already exists, and will be deleted", certificate.getCanonicalPath());
            FileUtils.forceDelete(certificate);
        }
        if (key.exists()) {
            LOGGER.info("Key file [{}] already exists, and will be deleted", certificate.getCanonicalPath());
            FileUtils.forceDelete(key);
        }
        try (val keyWriter = Files.newBufferedWriter(key.toPath(), StandardCharsets.UTF_8);
             val certWriter = Files.newBufferedWriter(certificate.toPath(), StandardCharsets.UTF_8)) {
            getConfigurationContext().getSamlIdPCertificateAndKeyWriter()
                .writeCertificateAndKey(keyWriter, certWriter);
        }
    }

    @Override
    public void afterPropertiesSet() {
        generate(Optional.empty());
    }

    /**
     * Initializes a new Generate saml metadata.
     */
    @SneakyThrows
    public void initialize() {
        getConfigurationContext().getSamlIdPMetadataLocator().initialize();
        generate(Optional.empty());
    }
}
