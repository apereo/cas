package org.apereo.cas.support.saml.web.idp.metadata;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import net.shibboleth.idp.installer.metadata.MetadataGenerator;
import net.shibboleth.idp.installer.metadata.MetadataGeneratorParameters;
import net.shibboleth.utilities.java.support.security.SelfSignedCertificateGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
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
    private transient Logger logger = LoggerFactory.getLogger(this.getClass());

    private File metadataFile;
    private File signingCertFile;
    private File signingKeyFile;

    private File encryptionCertFile;
    private File encryptionCertKeyFile;
    
    private File metadataLocation;
    
    private String entityId;
    
    private String hostName;
    
    private String scope;

    /**
     * Initializes a new Generate saml metadata.
     */
    @PostConstruct
    public void initialize() {
        Assert.notNull(this.metadataLocation, "IdP metadataLocation cannot be null and must be defined");
        Assert.hasText(this.entityId, "IdP entityID cannot be empty and must be defined");
        Assert.hasText(this.hostName, "IdP hostName cannot be empty and must be defined");
        Assert.hasText(this.scope, "IdP scope cannot be empty and must be defined");
        
        if (!this.metadataLocation.exists()) {
            if (!this.metadataLocation.mkdir()) {
                throw new IllegalArgumentException("Metadata directory location " + this.metadataLocation + " cannot be located/created");
            }
        }
        logger.info("Metadata directory location is at [{}] with entityID [{}]", this.metadataLocation, this.entityId);

        this.metadataFile = new File(this.metadataLocation, "idp-metadata.xml");
        this.signingCertFile = new File(this.metadataLocation, "idp-signing.crt");
        this.signingKeyFile = new File(this.metadataLocation, "idp-signing.key");

        this.encryptionCertFile = new File(this.metadataLocation, "idp-encryption.crt");
        this.encryptionCertKeyFile = new File(this.metadataLocation, "idp-encryption.key");
    }

    public File getMetadataFile() {
        return this.metadataFile;
    }

    public String getEntityId() {
        return this.entityId;
    }

    public String getScope() {
        return this.scope;
    }

    public String getHostName() {
        return this.hostName;
    }

    public File getSigningCertFile() {
        return this.signingCertFile;
    }

    public File getEncryptionCertFile() {
        return this.encryptionCertFile;
    }

    public boolean isMetadataMissing() {
        return !this.metadataFile.exists();
    }

    @Override
    public File performGenerationSteps() {
        try {
            logger.debug("Preparing to generate metadata for entityId [{}]", this.entityId);
            if (isMetadataMissing()) {
                logger.info("Metadata does not exist at [{}]. Creating...", this.metadataFile);

                logger.info("Creating self-sign certificate for signing...");
                buildSelfSignedSigningCert();

                logger.info("Creating self-sign certificate for encryption...");
                buildSelfSignedEncryptionCert();

                logger.info("Creating metadata...");
                buildMetadataGeneratorParameters();
            }

            logger.info("Metadata is available at [{}]", this.metadataFile);

            return this.metadataFile;
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            throw Throwables.propagate(e);
        }
    }

    /**
     * Build self signed encryption cert.
     *
     * @throws Exception the exception
     */
    protected void buildSelfSignedEncryptionCert() throws Exception {
        final SelfSignedCertificateGenerator generator = new SelfSignedCertificateGenerator();
        generator.setHostName(this.hostName);
        generator.setCertificateFile(this.encryptionCertFile);
        generator.setPrivateKeyFile(this.encryptionCertKeyFile);
        generator.setURISubjectAltNames(Lists.newArrayList(this.hostName.concat(URI_SUBJECT_ALTNAME_POSTFIX)));
        generator.generate();
    }

    /**
     * Build self signed signing cert.
     *
     * @throws Exception the exception
     */
    protected void buildSelfSignedSigningCert() throws Exception {
        final SelfSignedCertificateGenerator generator = new SelfSignedCertificateGenerator();
        generator.setHostName(this.hostName);
        generator.setCertificateFile(this.signingCertFile);
        generator.setPrivateKeyFile(this.signingKeyFile);
        generator.setURISubjectAltNames(Lists.newArrayList(this.hostName.concat(URI_SUBJECT_ALTNAME_POSTFIX)));
        generator.generate();
    }

    /**
     * Build metadata generator parameters by passing the encryption,
     * signing and back-channel certs to the parameter generator.
     * @throws IOException Thrown if cert files are missing, or metadata file inaccessible.
     */
    protected void buildMetadataGeneratorParameters() throws IOException {
        final MetadataGenerator generator = new MetadataGenerator(this.metadataFile);
        final MetadataGeneratorParameters parameters = new MetadataGeneratorParameters();

        parameters.setEncryptionCert(this.encryptionCertFile);
        parameters.setSigningCert(this.signingCertFile);

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

        generator.setDNSName(this.hostName);
        generator.setEntityID(this.entityId);
        generator.setScope(this.scope);
        generator.setSAML2AttributeQueryCommented(true);
        generator.setSAML2LogoutCommented(false);

        generator.generate();
    }

    public void setMetadataLocation(final File metadataLocation) {
        this.metadataLocation = metadataLocation;
    }

    public void setEntityId(final String entityId) {
        this.entityId = entityId;
    }

    public void setHostName(final String hostName) {
        this.hostName = hostName;
    }

    public void setScope(final String scope) {
        this.scope = scope;
    }
}
