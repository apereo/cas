package org.apereo.cas.configuration.model.support.cookie;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.Beans;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties class for tgc.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class TicketGrantingCookieProperties extends CookieProperties {

    /**
     * If remember-me is enabled, specifies the maximum age of the cookie.
     */
    private String rememberMeMaxAge = "P14D";

    @NestedConfigurationProperty
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

    public TicketGrantingCookieProperties() {
        super.setName("TGC");
    }

    public EncryptionJwtSigningJwtCryptographyProperties getCrypto() {
        return crypto;
    }

    public void setCrypto(final EncryptionJwtSigningJwtCryptographyProperties crypto) {
        this.crypto = crypto;
    }
    
    public long getRememberMeMaxAge() {
        return Beans.newDuration(rememberMeMaxAge).getSeconds();
    }

    public void setRememberMeMaxAge(final String rememberMeMaxAge) {
        this.rememberMeMaxAge = rememberMeMaxAge;
    }

}
