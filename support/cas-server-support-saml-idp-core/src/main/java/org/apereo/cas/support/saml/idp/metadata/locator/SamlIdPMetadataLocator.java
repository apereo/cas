package org.apereo.cas.support.saml.idp.metadata.locator;

import org.springframework.core.io.Resource;

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
    Resource getSigningCertificate();

    /**
     * Gets signing key file.
     *
     * @return the signing key file
     */
    Resource getSigningKey();

    /**
     * Gets idp metadata file.
     *
     * @return the metadata file
     */
    Resource getMetadata();

    /**
     * Gets encryption cert file.
     *
     * @return the encryption cert file
     */
    Resource getEncryptionCertificate();

    /**
     * Gets encryption key file.
     *
     * @return the encryption key file
     */
    Resource getEncryptionKey();

    /**
     * Initialize.
     */
    default void initialize() {}

    /**
     * Metadata exists?
     *
     * @return the boolean
     */
    boolean exists();
}
