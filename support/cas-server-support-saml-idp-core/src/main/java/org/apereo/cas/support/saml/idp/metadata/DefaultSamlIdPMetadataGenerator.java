package org.apereo.cas.support.saml.idp.metadata;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.shibboleth.utilities.java.support.security.SelfSignedCertificateGenerator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * A metadata generator based on a predefined template.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultSamlIdPMetadataGenerator implements SamlIdPMetadataGenerator {
    private static final String URI_SUBJECT_ALTNAME_POSTFIX = "/idp/metadata";

    private static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERTIFICATE = "-----END CERTIFICATE-----";

    private final File metadataLocation;
    private final String entityId;
    private final ResourceLoader resourceLoader;
    private final String casServerPrefix;
    private final String scope;

    /**
     * Initializes a new Generate saml metadata.
     */
    @PostConstruct
    @SneakyThrows
    public void initialize() {
        if (!metadataLocation.exists()) {
            LOGGER.debug("Metadata directory [{}] does not exist. Creating...", metadataLocation);
            if (!metadataLocation.mkdir()) {
                throw new IllegalArgumentException("Metadata directory location " + metadataLocation + " cannot be located/created");
            }
        }
        LOGGER.info("Metadata directory location is at [{}] with entity id [{}]", metadataLocation, this.entityId);

        generate();
    }

    /**
     * Is metadata missing?
     *
     * @return true/false
     */
    @SneakyThrows
    public boolean isMetadataMissing() {
        return !SamlIdPUtils.getIdPMetadataFile(metadataLocation).exists();
    }

    @Override
    @SneakyThrows
    public File generate() {
        LOGGER.debug("Preparing to generate metadata for entityId [{}]", this.entityId);
        final File metadataFile = SamlIdPUtils.getIdPMetadataFile(metadataLocation);
        if (isMetadataMissing()) {
            LOGGER.info("Metadata does not exist at [{}]. Creating...", metadataFile);

            LOGGER.info("Creating self-sign certificate for signing...");
            buildSelfSignedSigningCert();

            LOGGER.info("Creating self-sign certificate for encryption...");
            buildSelfSignedEncryptionCert();

            LOGGER.info("Creating metadata...");
            buildMetadataGeneratorParameters();
        }
        LOGGER.info("Metadata is available at [{}]", metadataFile);
        return metadataFile;
    }

    private String getIdPEndpointUrl() {
        return this.casServerPrefix.concat("/idp");
    }

    @SneakyThrows
    private String getIdPHostName() {
        final URL url = new URL(this.casServerPrefix);
        return url.getHost();
    }

    /**
     * Build self signed encryption cert.
     *
     * @throws Exception the exception
     */
    protected void buildSelfSignedEncryptionCert() throws Exception {
        final SelfSignedCertificateGenerator generator = new SelfSignedCertificateGenerator();
        generator.setHostName(getIdPHostName());
        final File encCert = SamlIdPUtils.getIdPEncryptionCertFile(this.metadataLocation).getFile();
        if (encCert.exists()) {
            FileUtils.forceDelete(encCert);
        }
        generator.setCertificateFile(encCert);
        final File encKey = SamlIdPUtils.getIdPEncryptionKeyFile(this.metadataLocation).getFile();
        if (encKey.exists()) {
            FileUtils.forceDelete(encKey);
        }
        generator.setPrivateKeyFile(encKey);
        generator.setURISubjectAltNames(CollectionUtils.wrap(getIdPHostName().concat(URI_SUBJECT_ALTNAME_POSTFIX)));
        generator.generate();
    }

    /**
     * Build self signed signing cert.
     *
     * @throws Exception the exception
     */
    protected void buildSelfSignedSigningCert() throws Exception {
        final SelfSignedCertificateGenerator generator = new SelfSignedCertificateGenerator();
        generator.setHostName(getIdPHostName());
        final File signingCert = SamlIdPUtils.getIdPSigningCertFile(this.metadataLocation).getFile();
        if (signingCert.exists()) {
            FileUtils.forceDelete(signingCert);
        }

        generator.setCertificateFile(signingCert);
        final File signingKey =SamlIdPUtils.getIdPSigningKeyFile(this.metadataLocation).getFile();
        if (signingKey.exists()) {
            FileUtils.forceDelete(signingKey);
        }
        generator.setPrivateKeyFile(signingKey);
        generator.setURISubjectAltNames(CollectionUtils.wrap(getIdPHostName().concat(URI_SUBJECT_ALTNAME_POSTFIX)));
        generator.generate();
    }

    /**
     * Build metadata generator parameters by passing the encryption,
     * signing and back-channel certs to the parameter generator.
     *
     * @throws Exception Thrown if cert files are missing, or metadata file inaccessible.
     */
    protected void buildMetadataGeneratorParameters() throws Exception {
        final Resource template = this.resourceLoader.getResource("classpath:/template-idp-metadata.xml");

        String signingCert = FileUtils.readFileToString(SamlIdPUtils.getIdPSigningCertFile(this.metadataLocation).getFile(), StandardCharsets.UTF_8);
        signingCert = StringUtils.remove(signingCert, BEGIN_CERTIFICATE);
        signingCert = StringUtils.remove(signingCert, END_CERTIFICATE).trim();

        String encryptionCert = FileUtils.readFileToString(SamlIdPUtils.getIdPEncryptionCertFile(this.metadataLocation).getFile(), StandardCharsets.UTF_8);
        encryptionCert = StringUtils.remove(encryptionCert, BEGIN_CERTIFICATE);
        encryptionCert = StringUtils.remove(encryptionCert, END_CERTIFICATE).trim();

        try (StringWriter writer = new StringWriter()) {
            IOUtils.copy(template.getInputStream(), writer, StandardCharsets.UTF_8);
            final String metadata = writer.toString()
                .replace("${entityId}", this.entityId)
                .replace("${scope}", this.scope)
                .replace("${idpEndpointUrl}", getIdPEndpointUrl())
                .replace("${encryptionKey}", encryptionCert)
                .replace("${signingKey}", signingCert);
            FileUtils.write(SamlIdPUtils.getIdPMetadataFile(this.metadataLocation), metadata, StandardCharsets.UTF_8);
        }
    }
}
