package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;

/**
 * This is {@link AuthyMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-authy")
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

    public String getMailAttribute() {
        return mailAttribute;
    }

    public void setMailAttribute(final String mailAttribute) {
        this.mailAttribute = mailAttribute;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(final String countryCode) {
        this.countryCode = countryCode;
    }

    public boolean isTrustedDeviceEnabled() {
        return trustedDeviceEnabled;
    }

    public void setTrustedDeviceEnabled(final boolean trustedDeviceEnabled) {
        this.trustedDeviceEnabled = trustedDeviceEnabled;
    }

    public String getPhoneAttribute() {
        return phoneAttribute;
    }

    public void setPhoneAttribute(final String phoneAttribute) {
        this.phoneAttribute = phoneAttribute;
    }
    
    public boolean isForceVerification() {
        return forceVerification;
    }

    public void setForceVerification(final boolean forceVerification) {
        this.forceVerification = forceVerification;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(final String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(final String apiUrl) {
        this.apiUrl = apiUrl;
    }
}

