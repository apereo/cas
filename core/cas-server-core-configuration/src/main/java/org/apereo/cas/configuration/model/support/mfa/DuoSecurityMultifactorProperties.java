package org.apereo.cas.configuration.model.support.mfa;

/**
 * This is {@link DuoSecurityMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DuoSecurityMultifactorProperties extends BaseMultifactorProvider {
    private static final long serialVersionUID = -4445375354167880807L;
    /**
     * Duo integration key.
     */
    private String duoIntegrationKey;
    /**
     * Duo secret key.
     */
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
    private String duoApplicationKey;
    /**
     * Duo API host and url.
     */
    private String duoApiHost;

    /**
     * Indicates whether this provider should support trusted devices.
     */
    private boolean trustedDeviceEnabled;

    public DuoSecurityMultifactorProperties() {
        setId("mfa-duo");
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
}


