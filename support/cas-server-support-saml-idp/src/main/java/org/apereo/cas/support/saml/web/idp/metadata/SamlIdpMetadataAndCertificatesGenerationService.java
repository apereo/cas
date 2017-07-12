package org.apereo.cas.support.saml.web.idp.metadata;

import java.io.File;

/**
 * This is {@link SamlIdpMetadataAndCertificatesGenerationService},
 * responsible for generating metadata and required certificates for signing and encryption.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@FunctionalInterface
public interface SamlIdpMetadataAndCertificatesGenerationService {

    /**
     * Perform the metadata generation steps.
     *
     * @return the metadata file
     */
    File performGenerationSteps();
}
