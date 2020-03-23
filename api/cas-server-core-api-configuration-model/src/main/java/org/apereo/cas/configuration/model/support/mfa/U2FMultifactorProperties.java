package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.couchdb.BaseAsynchronousCouchDbProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.model.support.quartz.ScheduledJobProperties;
import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RestEndpointProperties;
import org.apereo.cas.configuration.support.SpringResourceProperties;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.concurrent.TimeUnit;

/**
 * This is {@link U2FMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-u2f")
@Getter
@Setter
@Accessors(chain = true)
public class U2FMultifactorProperties extends BaseMultifactorProviderProperties {

    /**
     * Provider id by default.
     */
    public static final String DEFAULT_IDENTIFIER = "mfa-u2f";

    private static final long serialVersionUID = 6151350313777066398L;

    /**
     * Store device registration records inside a JDBC resource.
     */
    private Jpa jpa = new Jpa();

    /**
     * Expire and forget device registration requests after this period.
     */
    private long expireRegistrations = 30;

    /**
     * Device registration requests expiration time unit.
     */
    private TimeUnit expireRegistrationsTimeUnit = TimeUnit.SECONDS;

    /**
     * Expire and forget device registration records after this period.
     */
    private long expireDevices = 30;

    /**
     * Device registration record expiration time unit.
     */
    private TimeUnit expireDevicesTimeUnit = TimeUnit.DAYS;

    /**
     * Store device registration records inside a MongoDb resource.
     */
    private MongoDb mongo = new MongoDb();

    /**
     * Store device registration records inside a redis resource.
     */
    private Redis redis = new Redis();

    /**
     * Store device registration records inside a static JSON resource.
     */
    private Json json = new Json();

    /**
     * Store device registration records via a Groovy script.
     */
    private Groovy groovy = new Groovy();

    /**
     * Store device registration records via REST APIs.
     */
    private Rest rest = new Rest();

    /**
     * Indicates whether this provider should support trusted devices.
     */
    private boolean trustedDeviceEnabled;

    /**
     * Store device registration records via CouchDb.
     */
    private CouchDb couchDb = new CouchDb();

    /**
     * Clean up expired records via a background cleaner process.
     */
    @NestedConfigurationProperty
    private ScheduledJobProperties cleaner = new ScheduledJobProperties("PT10S", "PT1M");

    /**
     * Crypto settings that sign/encrypt the u2f registration records.
     */
    @NestedConfigurationProperty
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

    public U2FMultifactorProperties() {
        crypto.getEncryption().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
        setId(DEFAULT_IDENTIFIER);
    }

    @RequiresModule(name = "cas-server-support-u2f-couchdb")
    public static class CouchDb extends BaseAsynchronousCouchDbProperties {

        private static final long serialVersionUID = 2751957521987245445L;

        public CouchDb() {
            setDbName("u2f_multifactor");
        }
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Jpa extends AbstractJpaProperties {

        private static final long serialVersionUID = -4334840263678287815L;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class MongoDb extends SingleCollectionMongoDbProperties {

        private static final long serialVersionUID = -7963843335569634144L;

        public MongoDb() {
            setCollection("CasMongoDbU2FRepository");
        }
    }

    @RequiresModule(name = "cas-server-support-u2f")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Json extends SpringResourceProperties {

        private static final long serialVersionUID = -6883660787308509919L;
    }

    @RequiresModule(name = "cas-server-support-u2f")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Rest extends RestEndpointProperties {

        private static final long serialVersionUID = -8102345678378393382L;
    }

    @RequiresModule(name = "cas-server-support-u2f")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Groovy extends SpringResourceProperties {

        private static final long serialVersionUID = 8079027843747126083L;
    }

    @RequiresModule(name = "cas-server-support-u2f-redis")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Redis extends BaseRedisProperties {
        private static final long serialVersionUID = -1261683393319585262L;
    }
}
