package org.apereo.cas.configuration.model.support.consent;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.support.SpringResourceProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;

/**
 * This is {@link ConsentProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ConsentProperties implements Serializable {
    private static final long serialVersionUID = 5201308051524438384L;
    /**
     * Global reminder time unit, to reconfirm consent
     * in cases no changes are detected.
     */
    private int reminder = 30;
    /**
     * Global reminder time unit of measure, to reconfirm consent
     * in cases no changes are detected.
     */
    private ChronoUnit reminderTimeUnit = ChronoUnit.DAYS;

    /**
     * Keep consent decisions stored via REST.
     */
    private Rest rest = new Rest();

    /**
     * Keep consent decisions stored via JDBC resources.
     */
    private Jpa jpa = new Jpa();

    /**
     * Keep consent decisions stored via a static JSON resource.
     */
    private Json json = new Json();

    /**
     * Keep consent decisions stored via a MongoDb database resource.
     */
    private Mongodb mongo = new Mongodb();
    
    /**
     * Signing/encryption settings.
     */
    @NestedConfigurationProperty
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();
    
    public EncryptionJwtSigningJwtCryptographyProperties getCrypto() {
        return crypto;
    }

    public void setCrypto(final EncryptionJwtSigningJwtCryptographyProperties crypto) {
        this.crypto = crypto;
    }

    public Json getJson() {
        return json;
    }

    public void setJson(final Json json) {
        this.json = json;
    }

    public Jpa getJpa() {
        return jpa;
    }

    public void setJpa(final Jpa jpa) {
        this.jpa = jpa;
    }

    public int getReminder() {
        return reminder;
    }

    public void setReminder(final int reminder) {
        this.reminder = reminder;
    }

    public ChronoUnit getReminderTimeUnit() {
        return reminderTimeUnit;
    }

    public void setReminderTimeUnit(final ChronoUnit reminderTimeUnit) {
        this.reminderTimeUnit = reminderTimeUnit;
    }

    public Rest getRest() {
        return rest;
    }

    public void setRest(final Rest rest) {
        this.rest = rest;
    }

    public Mongodb getMongo() {
        return mongo;
    }

    public void setMongo(final Mongodb mongo) {
        this.mongo = mongo;
    }

    public static class Json extends SpringResourceProperties {
        private static final long serialVersionUID = 7079027843747126083L;
    }

    public static class Jpa extends AbstractJpaProperties {
        private static final long serialVersionUID = 1646689616653363554L;
    }

    public static class Mongodb extends SingleCollectionMongoDbProperties {
        private static final long serialVersionUID = -1918436901491275547L;

        public Mongodb() {
            setCollection("MongoDbCasConsentRepository");
        }
    }
    
    public static class Rest implements Serializable {
        private static final long serialVersionUID = -6909617495470495341L;

        /**
         * REST endpoint to use to which consent decision records will be submitted.
         */
        private String endpoint;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(final String endpoint) {
            this.endpoint = endpoint;
        }
    }
}
