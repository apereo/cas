package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.couchdb.BaseCouchDbProperties;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
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
    private Map<String, String> allowedDevices;

    /**
     * YubiKey API urls to contact for verification of credentials.
     */
    private List<String> apiUrls = new ArrayList<>();

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
     * Crypto settings that sign/encrypt the yubikey registration records.
     */
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

    public YubiKeyMultifactorProperties() {
        setId(DEFAULT_IDENTIFIER);
    }

    @RequiresModule(name = "cas-server-support-yubikey-couchdb")
    @Getter
    @Setter
    public static class CouchDb extends BaseCouchDbProperties {

        private static final long serialVersionUID = 3757390989294642185L;

        public CouchDb() {
            this.setDbName("yubikey");
        }
    }

    @Getter
    @Setter
    public static class Jpa extends AbstractJpaProperties {

        private static final long serialVersionUID = -4420099402220880361L;
    }

    @Getter
    @Setter
    public static class MongoDb extends SingleCollectionMongoDbProperties {

        private static final long serialVersionUID = 6876845341227039713L;

        public MongoDb() {
            setCollection("MongoDbYubiKeyRepository");
        }
    }
}
