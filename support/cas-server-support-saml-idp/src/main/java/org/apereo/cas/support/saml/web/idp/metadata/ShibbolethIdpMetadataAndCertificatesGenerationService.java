package org.apereo.cas.support.saml.web.idp.metadata;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import net.shibboleth.idp.installer.metadata.MetadataGenerator;
import net.shibboleth.idp.installer.metadata.MetadataGeneratorParameters;
import net.shibboleth.utilities.java.support.security.SelfSignedCertificateGenerator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A metadata generator based on the Shibboleth IdP's {@link MetadataGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class ShibbolethIdpMetadataAndCertificatesGenerationService implements SamlIdpMetadataAndCertificatesGenerationService {
    private static final String URI_SUBJECT_ALTNAME_POSTFIX = "idp/metadata";
    private static final Logger LOGGER = LoggerFactory.getLogger(ShibbolethIdpMetadataAndCertificatesGenerationService.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    /**
     * Initializes a new Generate saml metadata.
     */
    @PostConstruct
    public void initialize() {

        try {
            final SamlIdPProperties idp = casProperties.getAuthn().getSamlIdp();
            final Resource metadataLocation = idp.getMetadata().getLocation();

            if (!metadataLocation.exists()) {
                if (!metadataLocation.getFile().mkdir()) {
                    throw new IllegalArgumentException("Metadata directory location " + metadataLocation + " cannot be located/created");
                }
            }
            LOGGER.info("Metadata directory location is at [{}] with entityID [{}]", metadataLocation, idp.getEntityId());
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }


    /**
     * Is metadata missing?
     *
     * @return true/false
     */
    public boolean isMetadataMissing() {
        try {
            final SamlIdPProperties idp = casProperties.getAuthn().getSamlIdp();
            return !idp.getMetadata().getMetadataFile().exists();
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public File performGenerationSteps() {
        try {
            final SamlIdPProperties idp = casProperties.getAuthn().getSamlIdp();
            LOGGER.debug("Preparing to generate metadata for entityId [{}]", idp.getEntityId());
            if (isMetadataMissing()) {
                LOGGER.info("Metadata does not exist at [{}]. Creating...", idp.getMetadata().getMetadataFile());

                LOGGER.info("Creating self-sign certificate for signing...");
                buildSelfSignedSigningCert();

                LOGGER.info("Creating self-sign certificate for encryption...");
                buildSelfSignedEncryptionCert();

                LOGGER.info("Creating metadata...");
                buildMetadataGeneratorParameters();
            }

            LOGGER.info("Metadata is available at [{}]", idp.getMetadata().getMetadataFile());

            return idp.getMetadata().getMetadataFile();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw Throwables.propagate(e);
        }
    }

    /**
     * Build self signed encryption cert.
     *
     * @throws Exception the exception
     */
    protected void buildSelfSignedEncryptionCert() throws Exception {
        final SamlIdPProperties idp = casProperties.getAuthn().getSamlIdp();
        final SelfSignedCertificateGenerator generator = new SelfSignedCertificateGenerator();
        generator.setHostName(idp.getHostName());
        generator.setCertificateFile(idp.getMetadata().getEncryptionCertFile().getFile());
        generator.setPrivateKeyFile(idp.getMetadata().getEncryptionKeyFile().getFile());
        generator.setURISubjectAltNames(Lists.newArrayList(idp.getHostName().concat(URI_SUBJECT_ALTNAME_POSTFIX)));
        generator.generate();
    }

    /**
     * Build self signed signing cert.
     *
     * @throws Exception the exception
     */
    protected void buildSelfSignedSigningCert() throws Exception {
        final SamlIdPProperties idp = casProperties.getAuthn().getSamlIdp();
        final SelfSignedCertificateGenerator generator = new SelfSignedCertificateGenerator();
        generator.setHostName(idp.getHostName());
        generator.setCertificateFile(idp.getMetadata().getSigningCertFile().getFile());
        generator.setPrivateKeyFile(idp.getMetadata().getSigningKeyFile().getFile());
        generator.setURISubjectAltNames(Lists.newArrayList(idp.getHostName().concat(URI_SUBJECT_ALTNAME_POSTFIX)));
        generator.generate();
    }

    /**
     * Build metadata generator parameters by passing the encryption,
     * signing and back-channel certs to the parameter generator.
     *
     * @throws Exception Thrown if cert files are missing, or metadata file inaccessible.
     */
    protected void buildMetadataGeneratorParameters() throws Exception {
        final SamlIdPProperties idp = casProperties.getAuthn().getSamlIdp();
        final MetadataGenerator generator = new MetadataGenerator(idp.getMetadata().getMetadataFile());
        final MetadataGeneratorParameters parameters = new MetadataGeneratorParameters();

        parameters.setEncryptionCert(idp.getMetadata().getEncryptionCertFile().getFile());
        parameters.setSigningCert(idp.getMetadata().getSigningCertFile().getFile());

        final List<List<String>> signing = new ArrayList<>(2);
        List<String> value = parameters.getBackchannelCert();
        if (null != value) {
            signing.add(value);
        }
        value = parameters.getSigningCert();
        if (null != value) {
            signing.add(value);
        }
        generator.setSigningCerts(signing);
        value = parameters.getEncryptionCert();
        if (null != value) {
            generator.setEncryptionCerts(Collections.singletonList(value));
        }

        generator.setDNSName(idp.getHostName());
        generator.setEntityID(idp.getEntityId());
        generator.setScope(idp.getScope());
        generator.setSAML2AttributeQueryCommented(true);
        generator.setSAML2LogoutCommented(false);

        generator.generate();
    }
}
