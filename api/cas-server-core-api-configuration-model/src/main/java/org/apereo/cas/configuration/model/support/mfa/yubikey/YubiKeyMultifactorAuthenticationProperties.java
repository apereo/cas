package org.apereo.cas.configuration.model.support.mfa.yubikey;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link YubiKeyMultifactorAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-yubikey")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("YubiKeyMultifactorProperties")
public class YubiKeyMultifactorAuthenticationProperties extends BaseMultifactorAuthenticationProviderProperties {

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
     * When enabled, allows the user/system to accept multiple accounts
     * and device registrations per user, allowing one to switch between
     * or register new devices/accounts automatically.
     */
    private boolean multipleDeviceRegistrationEnabled;
    
    /**
     * Keep device registration records inside a static JSON resource.
     */
    private transient Resource jsonFile;

    /**
     * Collection of allowed devices allowed per user.
     * This is done using a key-value structure where the key is the user
     * the value is the allowed collection of yubikey device ids.
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
    @NestedConfigurationProperty
    private YubiKeyCouchDbMultifactorProperties couchDb = new YubiKeyCouchDbMultifactorProperties();

    /**
     * Keep device registration records inside a JDBC resource.
     */
    @NestedConfigurationProperty
    private YubiKeyJpaMultifactorProperties jpa = new YubiKeyJpaMultifactorProperties();

    /**
     * Keep device registration records inside a MongoDb resource.
     */
    @NestedConfigurationProperty
    private YubiKeyMongoDbMultifactorProperties mongo = new YubiKeyMongoDbMultifactorProperties();

    /**
     * Keep device registration records inside a redis resource.
     */
    @NestedConfigurationProperty
    private YubiKeyRedisMultifactorProperties redis = new YubiKeyRedisMultifactorProperties();

    /**
     * Keep device registration records inside a dynamo db resource.
     */
    @NestedConfigurationProperty
    private YubiKeyDynamoDbMultifactorProperties dynamoDb = new YubiKeyDynamoDbMultifactorProperties();

    /**
     * Keep device registration records inside a rest api.
     */
    @NestedConfigurationProperty
    private YubiKeyRestfulMultifactorProperties rest = new YubiKeyRestfulMultifactorProperties();

    /**
     * Crypto settings that sign/encrypt the yubikey registration records.
     */
    @NestedConfigurationProperty
    private EncryptionJwtSigningJwtCryptographyProperties crypto =
        new EncryptionJwtSigningJwtCryptographyProperties();

    public YubiKeyMultifactorAuthenticationProperties() {
        setId(DEFAULT_IDENTIFIER);
        crypto.getEncryption().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }

}
