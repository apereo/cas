package org.apereo.cas.configuration.model.support.mfa;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.model.support.mongo.SingleCollectionMongoDbProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;
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
    private Integer clientId;
    /**
     *  Yubikey secret key.
     */
    @RequiredProperty
    private String secretKey = StringUtils.EMPTY;

    /**
     * Keep device registration records inside a static JSON resource.
     */
    private Resource jsonFile;
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
     * Keep device registration records inside a JDBC resource.
     */
    private Jpa jpa = new Jpa();

    /**
     * Keep device registration records inside a MongoDb resource.
     */
    private MongoDb mongo = new MongoDb();

    public YubiKeyMultifactorProperties() {
        setId(DEFAULT_IDENTIFIER);
    }

    public boolean isTrustedDeviceEnabled() {
        return trustedDeviceEnabled;
    }

    public void setTrustedDeviceEnabled(final boolean trustedDeviceEnabled) {
        this.trustedDeviceEnabled = trustedDeviceEnabled;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(final Integer clientId) {
        this.clientId = clientId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(final String secretKey) {
        this.secretKey = secretKey;
    }

    public List<String> getApiUrls() {
        return apiUrls;
    }

    public void setApiUrls(final List<String> apiUrls) {
        this.apiUrls = apiUrls;
    }

    public Resource getJsonFile() {
        return jsonFile;
    }

    public void setJsonFile(final Resource jsonFile) {
        this.jsonFile = jsonFile;
    }

    public Map<String, String> getAllowedDevices() {
        return allowedDevices;
    }

    public void setAllowedDevices(final Map<String, String> allowedDevices) {
        this.allowedDevices = allowedDevices;
    }

    public Jpa getJpa() {
        return jpa;
    }

    public void setJpa(final Jpa jpa) {
        this.jpa = jpa;
    }

    public MongoDb getMongo() {
        return mongo;
    }

    public void setMongo(final MongoDb mongodb) {
        this.mongo = mongodb;
    }

    public static class Jpa extends AbstractJpaProperties {
        private static final long serialVersionUID = -4420099402220880361L;
    }

    public static class MongoDb extends SingleCollectionMongoDbProperties {
        private static final long serialVersionUID = 6876845341227039713L;

        public MongoDb() {
            setCollection("MongoDbYubiKeyRepository");
        }
    }
}

