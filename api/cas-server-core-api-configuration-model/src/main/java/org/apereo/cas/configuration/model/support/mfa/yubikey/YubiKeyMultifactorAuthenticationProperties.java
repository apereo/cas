package org.apereo.cas.configuration.model.support.mfa.yubikey;

import module java.base;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtCryptoProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.core.util.SigningJwtCryptoProperties;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

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
public class YubiKeyMultifactorAuthenticationProperties extends BaseMultifactorAuthenticationProviderProperties {

    /**
     * Provider id by default.
     */
    public static final String DEFAULT_IDENTIFIER = "mfa-yubikey";

    @Serial
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
    @NonNull
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
    @ExpressionLanguageCapable
    private YubiKeyJsonMultifactorProperties json = new YubiKeyJsonMultifactorProperties();

    /**
     * Collection of allowed devices allowed per user.
     * This is done using a key-value structure where the key is the user
     * the value is the allowed collection of yubikey device ids.
     */
    private Map<String, String> allowedDevices = new LinkedHashMap<>(1);

    /**
     * YubiKey API urls to contact for verification of credentials.
     */
    private List<String> apiUrls = new ArrayList<>();

    /**
     * Indicates whether this provider should support trusted devices.
     */
    private boolean trustedDeviceEnabled;

    /**
     * Define the strategy that controls how devices should be validated.
     */
    private YubiKeyDeviceValidationOptions validator = YubiKeyDeviceValidationOptions.VERIFY;

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
        crypto.getEncryption().setKeySize(EncryptionJwtCryptoProperties.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(SigningJwtCryptoProperties.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }

    /**
     * Device validation options.
     */
    public enum YubiKeyDeviceValidationOptions {
        /**
         * Verify yubikey devices via YubiKey APIs.
         */
        VERIFY,
        /**
         * Skip all validations checks and accept all devices.
         */
        SKIP,
        /**
         * Reject all devices.
         */
        REJECT
    }
}
