package org.apereo.cas.configuration.model.core.ticket;

import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link SigningEncryptionProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SigningEncryptionProperties {
    private String encryptionKey = StringUtils.EMPTY;

    private String signingKey = StringUtils.EMPTY;

    private boolean cipherEnabled;

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
}
