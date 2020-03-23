package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.couchdb.BaseCouchDbProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.model.support.redis.BaseRedisProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link YubiKeyMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-yubikey")
@Getter
@Setter
public class YubiKeyMultifactorProperties extends BaseMultifactorProviderProperties {

    /**
     * Provider id by default.
     */
    public static final String DEFAULT_IDENTIFIER = "mfa-yubikey";

    private static final long serialVersionUID = 9138057706201201089L;

    /**
     * Yubikey client id.
     */
    @RequiredProperty
    private Integer clientId = 0;

    /**
     * Yubikey secret key.
     */
    @RequiredProperty
    private String secretKey = StringUtils.EMPTY;

    /**
     * Keep device registration records inside a static JSON resource.
     */
    private transient Resource jsonFile;

    /**
     * Collection of allowed devices whitelisted per user.
     * This is done using a key-value structure where the key is the user
     * the value is the whitelisted collection of yubikey device ids.
     */
    private Map<String, String> allowedDevices = new LinkedHashMap<>(1);

    /**
     * YubiKey API urls to contact for verification of credentials.
     */
    private List<String> apiUrls = new ArrayList<>(0);

    /**
     * Indicates whether this provider should support trusted devices.
     */
    private boolean trustedDeviceEnabled;

    /**
     * Keep device registration records inside a CouchDb resource.
     */
    private CouchDb couchDb = new CouchDb();

    /**
     * Keep device registration records inside a JDBC resource.
     */
    private Jpa jpa = new Jpa();

    /**
     * Keep device registration records inside a MongoDb resource.
     */
    private MongoDb mongo = new MongoDb();

    /**
     * Keep device registration records inside a redis resource.
     */
    private Redis redis = new Redis();

    /**
     * Crypto settings that sign/encrypt the yubikey registration records.
     */
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

    public YubiKeyMultifactorProperties() {
        setId(DEFAULT_IDENTIFIER);
        crypto.getEncryption().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }

    @RequiresModule(name = "cas-server-support-yubikey-couchdb")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class CouchDb extends BaseCouchDbProperties {

        private static final long serialVersionUID = 3757390989294642185L;

        public CouchDb() {
            this.setDbName("yubikey");
        }
    }

    @RequiresModule(name = "cas-server-support-yubikey-jpa")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Jpa extends AbstractJpaProperties {

        private static final long serialVersionUID = -4420099402220880361L;
    }

    @RequiresModule(name = "cas-server-support-yubikey-mongo")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class MongoDb extends SingleCollectionMongoDbProperties {

        private static final long serialVersionUID = 6876845341227039713L;

        public MongoDb() {
            setCollection("MongoDbYubiKeyRepository");
        }
    }

    @RequiresModule(name = "cas-server-support-yubikey-redis")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Redis extends BaseRedisProperties {
        private static final long serialVersionUID = -1261683393319585262L;
    }
}
