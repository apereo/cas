package org.apereo.cas.configuration.model.support.clearpass;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link ClearpassProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class ClearpassProperties implements Serializable {
    private static final long serialVersionUID = 6047778458053531460L;
    /**
     * Enable clearpass and allow CAS to cache credentials.
     */
    private boolean cacheCredential;

    /**
     * Crypto settings that sign/encrypt the password captured.
     */
    @NestedConfigurationProperty
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

    public boolean isCacheCredential() {
        return cacheCredential;
    }

    public void setCacheCredential(final boolean cacheCredential) {
        this.cacheCredential = cacheCredential;
    }

    public EncryptionJwtSigningJwtCryptographyProperties getCrypto() {
        return crypto;
    }

    public void setCrypto(final EncryptionJwtSigningJwtCryptographyProperties crypto) {
        this.crypto = crypto;
    }
}
