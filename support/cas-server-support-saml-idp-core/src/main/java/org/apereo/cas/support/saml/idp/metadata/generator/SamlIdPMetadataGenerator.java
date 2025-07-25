package org.apereo.cas.support.saml.idp.metadata.generator;

import org.apereo.cas.support.saml.SamlIdPUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlIdPMetadataDocument;
import org.apache.commons.lang3.Strings;
import org.springframework.core.io.Resource;
import java.util.Optional;

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
     * Bean implementation id.
     */
    String BEAN_NAME = "samlIdPMetadataGenerator";

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
     * @param registeredService the registered service
     * @return the saml idp metadata document
     * @throws Throwable the throwable
     */
    SamlIdPMetadataDocument generate(Optional<SamlRegisteredService> registeredService) throws Throwable;

    /**
     * Clean certificate string.
     *
     * @param cert the cert
     * @return the string
     */
    static String cleanCertificate(final String cert) {
        var result = Strings.CI.remove(cert, BEGIN_CERTIFICATE);
        result = Strings.CI.remove(result, END_CERTIFICATE).trim();
        return result;
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

    record CertificateAndKey(Resource certificate, Resource key) {
    }
}
