package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.mongo.AbstractMongoClientProperties;
import org.apereo.cas.configuration.model.support.quartz.SchedulingProperties;
import org.apereo.cas.configuration.support.Beans;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link TrustedDevicesMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
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
     * Settings that control the background cleaner process.
     */
    private Cleaner cleaner = new Cleaner();
    /**
     * Store devices records inside MongoDb.
     */
    private MongoDb mongodb = new MongoDb();

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

    public MongoDb getMongodb() {
        return mongodb;
    }

    public void setMongodb(final MongoDb mongodb) {
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

    public static class MongoDb extends AbstractMongoClientProperties {
        private static final long serialVersionUID = 4940497540189318943L;

        public MongoDb() {
            setCollection("MongoDbCasTrustedAuthnMfaRepository");
        }
    }

    public static class Cleaner implements Serializable {

        private static final long serialVersionUID = 751673609815531025L;

        @NestedConfigurationProperty
        private SchedulingProperties schedule = new SchedulingProperties();

        public Cleaner() {
            schedule.setEnabled(true);
            schedule.setStartDelay("PT15S");
            schedule.setRepeatInterval("PT2M");
        }

        public SchedulingProperties getSchedule() {
            return schedule;
        }

        public void setSchedule(final SchedulingProperties schedule) {
            this.schedule = schedule;
        }
    }
}
