package org.apereo.cas.configuration.model.support.cookie;

/**
 * Configuration properties class for tgc.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class TicketGrantingCookieProperties extends CookieProperties {

    private int rememberMeMaxAge = 1209600;

    private String encryptionKey = "";

    private String signingKey = "";
    
    private boolean cipherEnabled = true;
    
    public TicketGrantingCookieProperties() {
        super.setName("TGC");
    }
    
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

    public int getRememberMeMaxAge() {
        return rememberMeMaxAge;
    }

    public void setRememberMeMaxAge(final int rememberMeMaxAge) {
        this.rememberMeMaxAge = rememberMeMaxAge;
    }

    public boolean isCipherEnabled() {
        return cipherEnabled;
    }

    public void setCipherEnabled(final boolean cipherEnabled) {
        this.cipherEnabled = cipherEnabled;
    }
}
