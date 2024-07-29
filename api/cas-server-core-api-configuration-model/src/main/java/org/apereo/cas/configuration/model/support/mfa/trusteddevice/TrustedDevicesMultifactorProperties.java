package org.apereo.cas.configuration.model.support.mfa.trusteddevice;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtCryptoProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.core.util.SigningJwtCryptoProperties;
import org.apereo.cas.configuration.model.support.dynamodb.DynamoDbTrustedDevicesMultifactorProperties;
import org.apereo.cas.configuration.model.support.quartz.ScheduledJobProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link TrustedDevicesMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-trusted-mfa")
@Getter
@Setter
@Accessors(chain = true)

public class TrustedDevicesMultifactorProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 1505013239016790473L;

    /**
     * Trusted devices core settings.
     */
    @NestedConfigurationProperty
    private TrustedDevicesMultifactorCoreProperties core = new TrustedDevicesMultifactorCoreProperties();

    /**
     * Store devices records via REST.
     */
    @NestedConfigurationProperty
    private RestfulTrustedDevicesMultifactorProperties rest = new RestfulTrustedDevicesMultifactorProperties();

    /**
     * Store devices records via JDBC resources.
     */
    @NestedConfigurationProperty
    private JpaTrustedDevicesMultifactorProperties jpa = new JpaTrustedDevicesMultifactorProperties();

    /**
     * Record trusted devices via a JSON resource.
     */
    @NestedConfigurationProperty
    private JsonTrustedDevicesMultifactorProperties json = new JsonTrustedDevicesMultifactorProperties();

    /**
     * Configure how device fingerprints are generated.
     */
    @NestedConfigurationProperty
    private DeviceFingerprintProperties deviceFingerprint = new DeviceFingerprintProperties();

    /**
     * Settings that control the background cleaner process.
     */
    @NestedConfigurationProperty
    private ScheduledJobProperties cleaner = new ScheduledJobProperties();

    /**
     * Store devices records inside MongoDb.
     */
    @NestedConfigurationProperty
    private MongoDbTrustedDevicesMultifactorProperties mongo = new MongoDbTrustedDevicesMultifactorProperties();

    /**
     * Store devices records inside DynamoDb.
     */
    @NestedConfigurationProperty
    private DynamoDbTrustedDevicesMultifactorProperties dynamoDb = new DynamoDbTrustedDevicesMultifactorProperties();

    /**
     * Store devices records inside Redis.
     */
    @NestedConfigurationProperty
    private RedisTrustedDevicesMultifactorProperties redis = new RedisTrustedDevicesMultifactorProperties();

    /**
     * Crypto settings that sign/encrypt the device records.
     */
    @NestedConfigurationProperty
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

    public TrustedDevicesMultifactorProperties() {
        crypto.getEncryption().setKeySize(EncryptionJwtCryptoProperties.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(SigningJwtCryptoProperties.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
        cleaner.getSchedule().setEnabled(true).setStartDelay("PT1M").setRepeatInterval("PT1M");
    }
}
