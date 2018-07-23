package org.apereo.cas.configuration.model.support.pac4j;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.cookie.CookieProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link Pac4jDelegatedSessionCookieProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
public class Pac4jDelegatedSessionCookieProperties extends CookieProperties {
    private static final long serialVersionUID = 7392972818105536350L;

    /**
     * Crypto settings that determine how the cookie should be signed and encrypted.
     */
    @NestedConfigurationProperty
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

    public Pac4jDelegatedSessionCookieProperties() {
        super.setName("PAC4JDELSESSION");
    }
}
