package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.model.support.quartz.ScheduledJobProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.SpringResourceProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link GAuthMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-gauth")
public class GAuthMultifactorProperties extends BaseMultifactorProviderProperties {
    /**
     * Provider id by default.
     */
    public static final String DEFAULT_IDENTIFIER = "mfa-gauth";

    private static final long serialVersionUID = -7401748853833491119L;
    
    /**
     * Issuer used in the barcode when dealing with device registration events.
     * Used in the registration URL to identify CAS.
     */
    @RequiredProperty
    private String issuer = "CASIssuer";
    /**
     * Label used in the barcode when dealing with device registration events.
     * Used in the registration URL to identify CAS.
     */
    @RequiredProperty
    private String label = "CASLabel";
    /**
     * Length of the generated code.
     */
    private int codeDigits = 6;
    /**
     * The expiration time of the generated code in seconds.
     */
    private long timeStepSize = 30;
    /**
     * Since TOTP passwords are time-based, it is essential that the clock of both the server and
     * the client are synchronised within
     * the tolerance defined here as the window size.
     */
    private int windowSize = 3;

    /**
     * Store google authenticator devices inside a MongoDb instance.
     */
    private MongoDb mongo = new MongoDb();
    /**
     * Store google authenticator devices inside a jdbc instance.
     */
    private Jpa jpa = new Jpa();
    /**
     * Store google authenticator devices inside a json file.
     */
    private Json json = new Json();
    /**
     * Store google authenticator devices via a rest interface.
     */
    private Rest rest = new Rest();

    /**
     * Control how stale expired tokens should be cleared from the underlying store.
     */
    @NestedConfigurationProperty
    private ScheduledJobProperties cleaner = new ScheduledJobProperties("PT1M", "PT1M");

    public GAuthMultifactorProperties() {
        setId(DEFAULT_IDENTIFIER);
    }

    public Rest getRest() {
        return rest;
    }

    public void setRest(final Rest rest) {
        this.rest = rest;
    }

    public ScheduledJobProperties getCleaner() {
        return cleaner;
    }

    public void setCleaner(final ScheduledJobProperties cleaner) {
        this.cleaner = cleaner;
    }

    public Json getJson() {
        return json;
    }

    public void setJson(final Json json) {
        this.json = json;
    }

    public MongoDb getMongo() {
        return mongo;
    }

    public void setMongo(final MongoDb mongodb) {
        this.mongo = mongodb;
    }

    public Jpa getJpa() {
        return jpa;
    }

    public void setJpa(final Jpa jpa) {
        this.jpa = jpa;
    }

    public int getCodeDigits() {
        return codeDigits;
    }

    public void setCodeDigits(final int codeDigits) {
        this.codeDigits = codeDigits;
    }

    public long getTimeStepSize() {
        return timeStepSize;
    }

    public void setTimeStepSize(final long timeStepSize) {
        this.timeStepSize = timeStepSize;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(final int windowSize) {
        this.windowSize = windowSize;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(final String issuer) {
        this.issuer = issuer;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public static class Json extends SpringResourceProperties {
        private static final long serialVersionUID = 4303355159388663888L;
    }

    public static class Rest implements Serializable {
        private static final long serialVersionUID = 4518622579150572559L;
        /**
         * Endpoint url of the REST resource used for tokens that are kept to prevent replay attacks.
         */
        private String endpointUrl;

        public String getEndpointUrl() {
            return endpointUrl;
        }

        public void setEndpointUrl(final String endpointUrl) {
            this.endpointUrl = endpointUrl;
        }
    }

    public static class MongoDb extends SingleCollectionMongoDbProperties {
        private static final long serialVersionUID = -200556119517414696L;
        /**
         * Collection name where tokens are kept to prevent replay attacks.
         */
        private String tokenCollection;

        public MongoDb() {
            setCollection("MongoDbGoogleAuthenticatorRepository");
            setTokenCollection("MongoDbGoogleAuthenticatorTokenRepository");
        }

        public String getTokenCollection() {
            return tokenCollection;
        }

        public void setTokenCollection(final String tokenCollection) {
            this.tokenCollection = tokenCollection;
        }
    }

    public static class Jpa implements Serializable {
        private static final long serialVersionUID = -2689797889546802618L;
        /**
         * Database instance where tokens are kept to prevent replay attacks.
         */
        private Database database = new Database();

        public Database getDatabase() {
            return database;
        }

        public void setDatabase(final Database database) {
            this.database = database;
        }

        public static class Database extends AbstractJpaProperties {
            private static final long serialVersionUID = -7446381055350251885L;

            public Database() {
                super.setUrl("jdbc:hsqldb:mem:cas-gauth");
            }
        }
    }
}
