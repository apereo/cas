package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.model.support.quartz.ScheduledJobProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.SpringResourceProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link TrustedDevicesMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-trusted-mfa")
public class TrustedDevicesMultifactorProperties implements Serializable {
    private static final long serialVersionUID = 1505013239016790473L;
    /**
     * If an MFA request is bypassed due to a trusted authentication decision, applications will
     * receive a special attribute as part of the validation payload that indicates this behavior.
     * Applications must further account for the scenario where they ask for an MFA mode and
     * yet don’t receive confirmation of it in the response given the authentication
     * session was trusted and MFA bypassed.
     */
    private String authenticationContextAttribute = "isFromTrustedMultifactorAuthentication";

    /**
     * Indicates whether CAS should ask for device registration consent
     * or execute it automatically.
     */
    private boolean deviceRegistrationEnabled = true;
    /**
     * Indicates how long should record/devices be remembered as trusted devices.
     */
    private long expiration = 30;
    /**
     * Indicates the time unit by which record/devices are remembered as trusted devices.
     */
    private TimeUnit timeUnit = TimeUnit.DAYS;
    /**
     * Store devices records via REST.
     */
    private Rest rest = new Rest();
    /**
     * Store devices records via JDBC resources.
     */
    private Jpa jpa = new Jpa();

    /**
     * Record trusted devices via a JSON resource.
     */
    private Json json = new Json();
    
    /**
     * Settings that control the background cleaner process.
     */
    @NestedConfigurationProperty
    private ScheduledJobProperties cleaner = new ScheduledJobProperties("PT15S", "PT2M");
    /**
     * Store devices records inside MongoDb.
     */
    private MongoDb mongo = new MongoDb();

    /**
     * Crypto settings that sign/encrypt the device records.
     */
    @NestedConfigurationProperty
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();
    
    public Json getJson() {
        return json;
    }

    public void setJson(final Json json) {
        this.json = json;
    }

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

    public MongoDb getMongo() {
        return mongo;
    }

    public void setMongo(final MongoDb mongo) {
        this.mongo = mongo;
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

    public ScheduledJobProperties getCleaner() {
        return cleaner;
    }

    public void setCleaner(final ScheduledJobProperties cleaner) {
        this.cleaner = cleaner;
    }

    public static class Rest implements Serializable {
        private static final long serialVersionUID = 3659099897056632608L;
        /**
         * Endpoint where trusted device records will be submitted to.
         */
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

    public static class MongoDb extends SingleCollectionMongoDbProperties {
        private static final long serialVersionUID = 4940497540189318943L;

        public MongoDb() {
            setCollection("MongoDbCasTrustedAuthnMfaRepository");
        }
    }

    public static class Json extends SpringResourceProperties {
        private static final long serialVersionUID = 3599367681439517829L;
    }
}
