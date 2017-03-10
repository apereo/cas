package org.apereo.cas.configuration.model.support.clearpass;

import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link ClearpassProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class ClearpassProperties {
    private boolean cacheCredential;

    private String encryptionKey = StringUtils.EMPTY;

    private String signingKey = StringUtils.EMPTY;

    private boolean cipherEnabled = true;

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(final String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public String getSigningKey() {
        return signingKey;
    }

    public void setSigningKey(final String signingKey) {
        this.signingKey = signingKey;
    }

    public boolean isCipherEnabled() {
        return cipherEnabled;
    }

    public void setCipherEnabled(final boolean cipherEnabled) {
        this.cipherEnabled = cipherEnabled;
    }

    public boolean isCacheCredential() {
        return cacheCredential;
    }

    public void setCacheCredential(final boolean cacheCredential) {
        this.cacheCredential = cacheCredential;
    }
}
