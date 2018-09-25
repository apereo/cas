package org.apereo.cas.configuration.model.support.consent;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.couchdb.BaseCouchDbProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.ldap.AbstractLdapSearchProperties;
import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.SpringResourceProperties;

import lombok.Getter;
import lombok.Setter;
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
@Getter
@Setter
public class ConsentProperties implements Serializable {

    private static final long serialVersionUID = 5201308051524438384L;

    /**
     * Global reminder time unit, to reconfirm consent
     * in cases no changes are detected.
     */
    private long reminder = 30;

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
     * Keep consent decisions stored via a CouchDb database resource.
     */
    private CouchDb couchDb = new CouchDb();

    /**
     * Signing/encryption settings.
     */
    @NestedConfigurationProperty
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

    /**
     * This is {@link org.apereo.cas.configuration.model.support.couchdb.consent.CouchDbConsentProperties}.
     *
     * @author Timur Duehr
     * @since 5.3.0
     */
    @RequiresModule(name = "cas-server-support-consent-couchdb")
    @Getter
    @Setter
    public static class CouchDb extends BaseCouchDbProperties {
        private static final long serialVersionUID = 8184753250455916462L;

        public CouchDb() {
            this.setDbName("consent");
        }
    }

    @RequiresModule(name = "cas-server-consent-webflow")
    @Getter
    @Setter
    public static class Json extends SpringResourceProperties {

        private static final long serialVersionUID = 7079027843747126083L;
    }

    @RequiresModule(name = "cas-server-consent-webflow")
    @Getter
    @Setter
    public static class Groovy extends SpringResourceProperties {

        private static final long serialVersionUID = 7079027843747126083L;
    }

    @RequiresModule(name = "cas-server-consent-jdbc")
    @Getter
    @Setter
    public static class Jpa extends AbstractJpaProperties {

        private static final long serialVersionUID = 1646689616653363554L;
    }

    @RequiresModule(name = "cas-server-consent-ldap")
    @Getter
    @Setter
    public static class Ldap extends AbstractLdapSearchProperties {

        private static final long serialVersionUID = 1L;

        /**
         * Type of LDAP directory.
         */
        private LdapType type;

        /**
         * Name of LDAP attribute that holds consent decisions as JSON.
         */
        private String consentAttributeName = "casConsentDecision";
    }

    @RequiresModule(name = "cas-server-consent-mongo")
    @Getter
    @Setter
    public static class MongoDb extends SingleCollectionMongoDbProperties {

        private static final long serialVersionUID = -1918436901491275547L;

        public MongoDb() {
            setCollection("MongoDbCasConsentRepository");
        }
    }

    @RequiresModule(name = "cas-server-consent-rest")
    @Getter
    @Setter
    public static class Rest implements Serializable {

        private static final long serialVersionUID = -6909617495470495341L;

        /**
         * REST endpoint to use to which consent decision records will be submitted.
         */
        private String endpoint;
    }
}
