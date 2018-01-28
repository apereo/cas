package org.apereo.cas.support.saml.idp.metadata;

import org.springframework.core.io.Resource;

import java.io.File;

/**
 * This is {@link SamlIdPMetadataLocator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public interface SamlIdPMetadataLocator {
    /**
     * Gets full location of signing cert file.
     *
     * @return the signing cert file
     */
    Resource getIdPSigningCertFile();

    /**
     * Gets signing key file.
     *
     * @return the signing key file
     */
    Resource getIdPSigningKeyFile();

    /**
     * Gets idp metadata file.
     *
     * @return the metadata file
     */
    File getIdPMetadataFile();

    /**
     * Gets encryption cert file.
     *
     * @return the encryption cert file
     */
    Resource getIdPEncryptionCertFile();

    /**
     * Gets encryption key file.
     *
     * @return the encryption key file
     */
    Resource getIdPEncryptionKeyFile();

    /**
     * Gets configuration location.
     *
     * @return the configuration location
     */
    File getConfigurationLocation();

    /**
     * Initialize.
     */
    void initialize();
}
