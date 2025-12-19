package org.apereo.cas.support.saml.idp.metadata.locator;

import module java.base;
import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.springframework.core.io.Resource;

/**
 * This is {@link SamlIdPMetadataLocator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public interface SamlIdPMetadataLocator {

    /**
     * Bean definition name.
     */
    String BEAN_NAME = "samlIdPMetadataLocator";

    /**
     * Gets full location of signing cert file.
     *
     * @param registeredService the registered service
     * @return the signing cert file
     * @throws Throwable the throwable
     */
    Resource resolveSigningCertificate(Optional<SamlRegisteredService> registeredService) throws Throwable;

    /**
     * Gets signing key file.
     *
     * @param registeredService the registered service
     * @return the signing key file
     * @throws Throwable the throwable
     */
    Resource resolveSigningKey(Optional<SamlRegisteredService> registeredService) throws Throwable;

    /**
     * Gets idp metadata file.
     *
     * @param registeredService the registered service
     * @return the metadata file
     */
    Resource resolveMetadata(Optional<SamlRegisteredService> registeredService) throws Throwable;

    /**
     * Gets encryption cert file.
     *
     * @param registeredService the registered service
     * @return the encryption cert file
     */
    Resource resolveEncryptionCertificate(Optional<SamlRegisteredService> registeredService) throws Throwable;

    /**
     * Gets encryption key file.
     *
     * @param registeredService the registered service
     * @return the encryption key file
     */
    Resource resolveEncryptionKey(Optional<SamlRegisteredService> registeredService) throws Throwable;

    /**
     * Initialize.
     */
    default void initialize() {
    }

    /**
     * Metadata exists?
     *
     * @param registeredService the registered service
     * @return true/false
     */
    boolean exists(Optional<SamlRegisteredService> registeredService) throws Throwable;

    /**
     * Fetch metadata document.
     *
     * @param registeredService the registered service
     * @return the saml id p metadata document
     */
    SamlIdPMetadataDocument fetch(Optional<SamlRegisteredService> registeredService);

    /**
     * Should generate metadata for service?
     *
     * @param registeredService the registered service
     * @return true/false
     */
    default boolean shouldGenerateMetadataFor(final Optional<SamlRegisteredService> registeredService) {
        return registeredService.isEmpty();
    }

    /**
     * Gets applies to for.
     *
     * @param result the result
     * @return the applies to for
     */
    default String getAppliesToFor(final Optional<SamlRegisteredService> result) {
        return SamlIdPUtils.getSamlIdPMetadataOwner(result);
    }
}
