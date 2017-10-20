package org.apereo.cas.configuration.model.support.consent;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapProperties;
import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;
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
@RequiresModule(name = "cas-server-support-consent-webflow")
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
     * Keep consent decisions stored via LDAP user records.
     */
    private Ldap ldap = new Ldap();
    
    /**
     * Keep consent decisions stored via JDBC resources.
     */
    private Jpa jpa = new Jpa();

    /**
     * Keep consent decisions stored via a static JSON resource.
     */
    private Json json = new Json();

    /**
     * Keep consent decisions stored via a Groovy resource.
     */
    private Groovy groovy = new Groovy();

    /**
     * Keep consent decisions stored via a MongoDb database resource.
     */
    private MongoDb mongo = new MongoDb();
    
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

    public Groovy getGroovy() {
        return groovy;
    }

    public void setGroovy(final Groovy groovy) {
        this.groovy = groovy;
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

    public Ldap getLdap() {
        return ldap;
    }

    public void setLdap(final Ldap ldap) {
        this.ldap = ldap;
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

    public MongoDb getMongo() {
        return mongo;
    }

    public void setMongo(final MongoDb mongo) {
        this.mongo = mongo;
    }

    @RequiresModule(name = "cas-server-consent-webflow")
    public static class Json extends SpringResourceProperties {
        private static final long serialVersionUID = 7079027843747126083L;
    }

    @RequiresModule(name = "cas-server-consent-webflow")
    public static class Groovy extends SpringResourceProperties {
        private static final long serialVersionUID = 7079027843747126083L;
    }

    @RequiresModule(name = "cas-server-consent-jdbc")
    public static class Jpa extends AbstractJpaProperties {
        private static final long serialVersionUID = 1646689616653363554L;
    }

    @RequiresModule(name = "cas-server-consent-ldap")
    public static class Ldap extends AbstractLdapProperties {
        private static final long serialVersionUID = 1L;
        
        /**
         * Type of LDAP directory.
         */
        private LdapType type;
        
        /**
         * Name of LDAP attribute that holds consent decisions as JSON.
         */
        private String consentAttributeName = "casConsentDecision";
        /**
         * Whether subtree searching is allowed.
         */
        private boolean subtreeSearch = true;
        /**
         * Base DN to use.
         */
        private String baseDn;
        /**
         * User filter to use for searching.
         * Syntax is {@code cn={user}} or {@code cn={0}}.
         */
        private String userFilter;

        public LdapType getType() {
            return type;
        }

        public void setType(final LdapType type) {
            this.type = type;
        }
        
        public String getConsentAttributeName() {
            return consentAttributeName;
        }
        
        public void setConsentAttributeName(final String consentAttributeName) {
            this.consentAttributeName = consentAttributeName;
        }

        public boolean isSubtreeSearch() {
            return subtreeSearch;
        }

        public void setSubtreeSearch(final boolean subtreeSearch) {
            this.subtreeSearch = subtreeSearch;
        }

        public String getBaseDn() {
            return baseDn;
        }

        public void setBaseDn(final String baseDn) {
            this.baseDn = baseDn;
        }

        public String getUserFilter() {
            return userFilter;
        }

        public void setUserFilter(final String userFilter) {
            this.userFilter = userFilter;
        }
    }

    @RequiresModule(name = "cas-server-consent-mongo")
    public static class MongoDb extends SingleCollectionMongoDbProperties {
        private static final long serialVersionUID = -1918436901491275547L;

        public MongoDb() {
            setCollection("MongoDbCasConsentRepository");
        }
    }

    @RequiresModule(name = "cas-server-consent-rest")
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
