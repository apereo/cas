package org.apereo.cas.configuration.model.core.util;

/**
 * This is {@link EncryptionOptionalSigningJwtCryptographyProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class EncryptionOptionalSigningJwtCryptographyProperties extends EncryptionJwtSigningJwtCryptographyProperties {

    private static final long serialVersionUID = 7185404480671258520L;
    /**
     * Whether crypto encryption operations are enabled.
     */
    private boolean encryptionEnabled = true;

    public boolean isEncryptionEnabled() {
        return encryptionEnabled;
    }

    public void setEncryptionEnabled(final boolean encryptionEnabled) {
        this.encryptionEnabled = encryptionEnabled;
    }
}
