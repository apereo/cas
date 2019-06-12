package org.apereo.cas.support.saml.idp.metadata.generator;

import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

/**
 * This is {@link SamlIdPMetadataGenerator},
 * responsible for generating metadata and required certificates for signing and encryption.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@FunctionalInterface
public interface SamlIdPMetadataGenerator {

    /**
     * Perform the metadata generation steps.
     *
     * @return the saml id p metadata document
     */
    SamlIdPMetadataDocument generate();
}
