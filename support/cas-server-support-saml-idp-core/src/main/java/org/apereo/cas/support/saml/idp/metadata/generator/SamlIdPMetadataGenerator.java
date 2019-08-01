package org.apereo.cas.support.saml.idp.metadata.generator;

import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;

import org.apache.commons.lang3.StringUtils;

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
     * Starting block of a pem certificate.
     */
    String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
    /**
     * Ending block of a pem certificate.
     */
    String END_CERTIFICATE = "-----END CERTIFICATE-----";

    /**
     * Perform the metadata generation steps.
     *
     * @return the saml id p metadata document
     */
    SamlIdPMetadataDocument generate();

    static String cleanCertificate(final String cert) {
        var result = StringUtils.remove(cert, BEGIN_CERTIFICATE);
        result = StringUtils.remove(result, END_CERTIFICATE).trim();
        return result;
    }
}
