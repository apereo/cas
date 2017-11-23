package org.apereo.cas.configuration.model.support.cookie;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties class for tgc.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-cookie", automated = true)
public class TicketGrantingCookieProperties extends CookieProperties {

    private static final long serialVersionUID = 7392972818105536350L;
    /**
     * If remember-me is enabled, specifies the maximum age of the cookie.
     */
    private String rememberMeMaxAge = "P14D";

    /**
     * Crypto settings that determine how the cookie should be signed and encrypted.
     */
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
