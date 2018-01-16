package org.apereo.cas.configuration.model.support.saml.idp.metadata;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link SamlIdPMetadataProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-saml-idp")
@Slf4j
@Getter
@Setter
public class SamlIdPMetadataProperties implements Serializable {

    private static final long serialVersionUID = -1020542741768471305L;

    /**
     * Whether invalid metadata should eagerly fail quickly on startup
     * once the resource is parsed.
     */
    private boolean failFast = true;

    /**
     * Whether valid metadata is required.
     */
    private boolean requireValidMetadata = true;

    /**
     * How long should metadata be cached in minutes.
     */
    private long cacheExpirationMinutes = TimeUnit.DAYS.toMinutes(1);

    /**
     * Directory location of SAML metadata and signing/encryption keys.
     * This directory will be used to hold the configuration files.
     */
    @RequiredProperty
    private Resource location = new FileSystemResource("/etc/cas/saml");

    /**
     * Properties pertaining to mongo db saml metadata resolvers.
     */
    @NestedConfigurationProperty
    private MongoDbSamlMetadataProperties mongo = new MongoDbSamlMetadataProperties();

    /**
     * Properties pertaining to jpa metadata resolution.
     */
    @NestedConfigurationProperty
    private JpaSamlMetadataProperties jpa = new JpaSamlMetadataProperties();

    /**
     * Algorithm name to use when generating private key.
     */
    private String privateKeyAlgName = "RSA";

    /**
     * Basic auth username in case the metadata instance is connecting to an MDQ server.
     */
    private String basicAuthnUsername;

    /**
     * Basic auth password in case the metadata instance is connecting to an MDQ server.
     */
    private String basicAuthnPassword;

    /**
     * Supported content types in case the metadata instance is connecting to an MDQ server.
     */
    private List<String> supportedContentTypes = new ArrayList<>();

    /**
     * Gets full location of signing cert file.
     *
     * @return the signing cert file
     * @throws Exception the exception
     */
    public Resource getSigningCertFile() throws Exception {
        return new FileSystemResource(new File(this.location.getFile(), "/idp-signing.crt"));
    }

    /**
     * Gets signing key file.
     *
     * @return the signing key file
     * @throws Exception the exception
     */
    public Resource getSigningKeyFile() throws Exception {
        return new FileSystemResource(new File(this.location.getFile(), "/idp-signing.key"));
    }

    /**
     * Gets idp metadata file.
     *
     * @return the metadata file
     * @throws Exception the exception
     */
    public File getMetadataFile() throws Exception {
        return new File(this.location.getFile(), "idp-metadata.xml");
    }

    /**
     * Gets encryption cert file.
     *
     * @return the encryption cert file
     * @throws Exception the exception
     */
    public Resource getEncryptionCertFile() throws Exception {
        return new FileSystemResource(new File(this.location.getFile(), "/idp-encryption.crt"));
    }

    /**
     * Gets encryption key file.
     *
     * @return the encryption key file
     * @throws Exception the exception
     */
    public Resource getEncryptionKeyFile() throws Exception {
        return new FileSystemResource(new File(this.location.getFile(), "/idp-encryption.key"));
    }
}
