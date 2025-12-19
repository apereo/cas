package org.apereo.cas.support.saml.idp.metadata;

import module java.base;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This is {@link SamlIdPMetadataColumns}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Getter
@RequiredArgsConstructor
public enum SamlIdPMetadataColumns {

    /**
     * The owner of the metadata.
     */
    APPLIES_TO("appliesTo"),
    /**
     * The metadata itself.
     */
    METADATA("metadata"),
    /**
     * The signing certificate.
     */
    SIGNING_CERTIFICATE("signingCertificate"),
    /**
     * The signing key.
     */
    SIGNING_KEY("signingKey"),
    /**
     * The encryption certificate.
     */
    ENCRYPTION_CERTIFICATE("encryptionCertificate"),
    /**
     * The encryption key.
     */
    ENCRYPTION_KEY("encryptionKey");

    private final String columnName;
}
