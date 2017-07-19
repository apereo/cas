package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.mongo.AbstractMongoClientProperties;
import org.apereo.cas.configuration.support.Beans;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link TrustedDevicesProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class TrustedDevicesProperties {
    private static final long serialVersionUID = 1505013239016790473L;
    private String authenticationContextAttribute = "isFromTrustedMultifactorAuthentication";

    /**
     * Indicates whether CAS should ask for device registration consent
     * or execute it automatically.
     */
    private boolean deviceRegistrationEnabled = true;
    private long expiration = 30;
    private TimeUnit timeUnit = TimeUnit.DAYS;
    private Rest rest = new Rest();
    private Jpa jpa = new Jpa();
    private Cleaner cleaner = new Cleaner();
    private Mongodb mongodb = new Mongodb();

    @NestedConfigurationProperty
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();
    
    public EncryptionJwtSigningJwtCryptographyProperties getCrypto() {
        return crypto;
    }

    public void setCrypto(final EncryptionJwtSigningJwtCryptographyProperties crypto) {
        this.crypto = crypto;
    }

    public Rest getRest() {
        return rest;
    }

    public void setRest(final Rest rest) {
        this.rest = rest;
    }

    public Mongodb getMongodb() {
        return mongodb;
    }

    public void setMongodb(final Mongodb mongodb) {
        this.mongodb = mongodb;
    }

    public Jpa getJpa() {
        return jpa;
    }

    public void setJpa(final Jpa jpa) {
        this.jpa = jpa;
    }

    public long getExpiration() {
        return expiration;
    }

    public void setExpiration(final long expiration) {
        this.expiration = expiration;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(final TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public String getAuthenticationContextAttribute() {
        return authenticationContextAttribute;
    }

    public void setAuthenticationContextAttribute(final String authenticationContextAttribute) {
        this.authenticationContextAttribute = authenticationContextAttribute;
    }

    public boolean isDeviceRegistrationEnabled() {
        return deviceRegistrationEnabled;
    }

    public void setDeviceRegistrationEnabled(final boolean deviceRegistrationEnabled) {
        this.deviceRegistrationEnabled = deviceRegistrationEnabled;
    }

    public Cleaner getCleaner() {
        return cleaner;
    }

    public void setCleaner(final Cleaner cleaner) {
        this.cleaner = cleaner;
    }

    public static class Rest implements Serializable {
        private static final long serialVersionUID = 3659099897056632608L;
        private String endpoint;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(final String endpoint) {
            this.endpoint = endpoint;
        }
    }

    public static class Jpa extends AbstractJpaProperties {
        private static final long serialVersionUID = -8329950619696176349L;
    }

    public static class Mongodb extends AbstractMongoClientProperties {
        private static final long serialVersionUID = 4940497540189318943L;

        public Mongodb() {
            setCollection("MongoDbCasTrustedAuthnMfaRepository");
        }
    }

    public static class Cleaner {
        private boolean enabled = true;
        private String startDelay = "PT15S";

        private String repeatInterval = "PT2M";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        public long getStartDelay() {
            return Beans.newDuration(startDelay).toMillis();
        }

        public void setStartDelay(final String startDelay) {
            this.startDelay = startDelay;
        }

        public long getRepeatInterval() {
            return Beans.newDuration(repeatInterval).toMillis();
        }

        public void setRepeatInterval(final String repeatInterval) {
            this.repeatInterval = repeatInterval;
        }
    }
}
