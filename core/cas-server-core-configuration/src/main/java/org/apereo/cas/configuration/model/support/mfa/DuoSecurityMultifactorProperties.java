package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;

/**
 * This is {@link DuoSecurityMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-duo")
public class DuoSecurityMultifactorProperties extends BaseMultifactorProviderProperties {
    /**
     * Provider id by default.
     */
    public static final String DEFAULT_IDENTIFIER = "mfa-duo";
    
    private static final long serialVersionUID = -4655375354167880807L;
    
    /**
     * Duo integration key.
     */
    @RequiredProperty
    private String duoIntegrationKey;
    /**
     * Duo secret key.
     */
    @RequiredProperty
    private String duoSecretKey;
    /**
     * The duoApplicationKey is a string, at least 40 characters long,
     * that you generate and keep secret from Duo.
     * You can generate a random string in Python with:
     * <pre>
     * import os, hashlib
     * print hashlib.sha1(os.urandom(32)).hexdigest()
     * </pre>
     */
    @RequiredProperty
    private String duoApplicationKey;
    /**
     * Duo API host and url.
     */
    @RequiredProperty
    private String duoApiHost;

    /**
     * Link to a registration app, typically developed in-house
     * in order to allow new users to sign-up for duo functionality.
     * If the user account status requires enrollment and this link
     * is specified, CAS will redirect the authentication flow
     * to this registration app. Otherwise, the default duo mechanism
     * for new-user registrations shall take over.
     */
    private String registrationUrl;
    /**
     * Indicates whether this provider should support trusted devices.
     */
    private boolean trustedDeviceEnabled;

    public DuoSecurityMultifactorProperties() {
        setId(DEFAULT_IDENTIFIER);
    }

    public boolean isTrustedDeviceEnabled() {
        return trustedDeviceEnabled;
    }

    public void setTrustedDeviceEnabled(final boolean trustedDeviceEnabled) {
        this.trustedDeviceEnabled = trustedDeviceEnabled;
    }

    public String getDuoIntegrationKey() {
        return duoIntegrationKey;
    }

    public void setDuoIntegrationKey(final String duoIntegrationKey) {
        this.duoIntegrationKey = duoIntegrationKey;
    }

    public String getDuoSecretKey() {
        return duoSecretKey;
    }

    public void setDuoSecretKey(final String duoSecretKey) {
        this.duoSecretKey = duoSecretKey;
    }

    public String getDuoApplicationKey() {
        return duoApplicationKey;
    }

    public void setDuoApplicationKey(final String duoApplicationKey) {
        this.duoApplicationKey = duoApplicationKey;
    }

    public String getDuoApiHost() {
        return duoApiHost;
    }

    public void setDuoApiHost(final String duoApiHost) {
        this.duoApiHost = duoApiHost;
    }

    public String getRegistrationUrl() {
        return registrationUrl;
    }

    public void setRegistrationUrl(final String registrationUrl) {
        this.registrationUrl = registrationUrl;
    }
}
