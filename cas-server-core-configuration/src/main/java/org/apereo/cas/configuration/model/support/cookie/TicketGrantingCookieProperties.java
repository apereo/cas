package org.apereo.cas.configuration.model.support.cookie;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties class for tgc.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "tgc", ignoreUnknownFields = false)
public class TicketGrantingCookieProperties extends AbstractCookieProperties {

    public TicketGrantingCookieProperties() {
        super.setName("TGC");
    }

    private int rememberMeMaxAge = 1209600;

    private String encryptionKey = "";

    private String signingKey = "";

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
}
