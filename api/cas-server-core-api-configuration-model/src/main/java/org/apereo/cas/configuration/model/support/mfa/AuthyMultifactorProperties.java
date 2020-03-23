package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link AuthyMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-authy")
@Getter
@Setter
@Accessors(chain = true)
public class AuthyMultifactorProperties extends BaseMultifactorProviderProperties {

    /**
     * Provider id by default.
     */
    public static final String DEFAULT_IDENTIFIER = "mfa-authy";

    private static final long serialVersionUID = -3746749663459157641L;

    /**
     * Authy API key.
     */
    @RequiredProperty
    private String apiKey;

    /**
     * Authy API url.
     */
    @RequiredProperty
    private String apiUrl;

    /**
     * Principal attribute used to look up a phone number
     * for credential verification. The attribute value
     * is then used to look up the user record in Authy, or
     * create the user.
     */
    @RequiredProperty
    private String phoneAttribute = "phone";

    /**
     * Principal attribute used to look up an email address
     * for credential verification. The attribute value
     * is then used to look up the user record in Authy, or
     * create the user.
     */
    @RequiredProperty
    private String mailAttribute = "mail";

    /**
     * Phone number country code used to look up and/or create the Authy user account.
     */
    private String countryCode = "1";

    /**
     * Flag authentication requests to authy to force verification of credentials.
     */
    private boolean forceVerification = true;

    /**
     * Indicates whether this provider should support trusted devices.
     */
    private boolean trustedDeviceEnabled;

    public AuthyMultifactorProperties() {
        setId(DEFAULT_IDENTIFIER);
    }
}
