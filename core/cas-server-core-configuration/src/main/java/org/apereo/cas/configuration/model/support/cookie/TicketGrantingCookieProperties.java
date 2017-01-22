package org.apereo.cas.configuration.model.support.cookie;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.support.Beans;

/**
 * Configuration properties class for tgc.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class TicketGrantingCookieProperties extends CookieProperties {

    private String rememberMeMaxAge = "P14D";

    private String encryptionKey = StringUtils.EMPTY;

    private String signingKey = StringUtils.EMPTY;
    
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

    public long getRememberMeMaxAge() {
        return Beans.newDuration(rememberMeMaxAge).getSeconds();
    }

    public void setRememberMeMaxAge(final String rememberMeMaxAge) {
        this.rememberMeMaxAge = rememberMeMaxAge;
    }

    public boolean isCipherEnabled() {
        return cipherEnabled;
    }

    public void setCipherEnabled(final boolean cipherEnabled) {
        this.cipherEnabled = cipherEnabled;
    }
}
