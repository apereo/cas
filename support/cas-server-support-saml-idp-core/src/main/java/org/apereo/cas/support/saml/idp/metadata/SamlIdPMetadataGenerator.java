package org.apereo.cas.support.saml.idp.metadata;

import java.io.File;

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
     * @return the metadata file
     */
    File generate();
}
