package org.apereo.cas.support.saml.idp.metadata.locator;

import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import org.springframework.core.io.Resource;

import java.util.Optional;

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
     * @param registeredService the registered service
     * @return the signing cert file
     */
    Resource resolveSigningCertificate(Optional<SamlRegisteredService> registeredService);

    /**
     * Gets signing key file.
     *
     * @param registeredService the registered service
     * @return the signing key file
     */
    Resource resolveSigningKey(Optional<SamlRegisteredService> registeredService);

    /**
     * Gets idp metadata file.
     *
     * @param registeredService the registered service
     * @return the metadata file
     */
    Resource resolveMetadata(Optional<SamlRegisteredService> registeredService);

    /**
     * Gets encryption cert file.
     *
     * @param registeredService the registered service
     * @return the encryption cert file
     */
    Resource getEncryptionCertificate(Optional<SamlRegisteredService> registeredService);

    /**
     * Gets encryption key file.
     *
     * @param registeredService the registered service
     * @return the encryption key file
     */
    Resource resolveEncryptionKey(Optional<SamlRegisteredService> registeredService);

    /**
     * Initialize.
     */
    default void initialize() {}

    /**
     * Metadata exists?
     *
     * @param registeredService the registered service
     * @return true/false
     */
    boolean exists(Optional<SamlRegisteredService> registeredService);

    /**
     * Fetch metadata document.
     *
     * @param registeredService the registered service
     * @return the saml id p metadata document
     */
    SamlIdPMetadataDocument fetch(Optional<SamlRegisteredService> registeredService);
}
