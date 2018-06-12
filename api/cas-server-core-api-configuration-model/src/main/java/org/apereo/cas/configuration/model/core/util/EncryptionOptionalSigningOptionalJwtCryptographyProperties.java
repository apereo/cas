package org.apereo.cas.configuration.model.core.util;

import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link EncryptionOptionalSigningOptionalJwtCryptographyProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
public class EncryptionOptionalSigningOptionalJwtCryptographyProperties extends EncryptionJwtSigningJwtCryptographyProperties {

    private static final long serialVersionUID = 7185404480671258520L;

    /**
     * Whether crypto encryption operations are enabled.
     */
    private boolean encryptionEnabled = true;

    /**
     * Whether crypto signing operations are enabled.
     */
    private boolean signingEnabled = true;
}
