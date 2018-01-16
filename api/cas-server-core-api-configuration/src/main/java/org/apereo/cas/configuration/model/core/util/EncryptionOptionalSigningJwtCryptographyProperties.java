package org.apereo.cas.configuration.model.core.util;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link EncryptionOptionalSigningJwtCryptographyProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Getter
@Setter
public class EncryptionOptionalSigningJwtCryptographyProperties extends EncryptionJwtSigningJwtCryptographyProperties {

    private static final long serialVersionUID = 7185404480671258520L;

    /**
     * Whether crypto encryption operations are enabled.
     */
    private boolean encryptionEnabled = true;
}
