package org.apereo.cas.configuration.model.support.mfa.trusteddevice;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.dynamodb.DynamoDbTrustedDevicesMultifactorProperties;
import org.apereo.cas.configuration.model.support.quartz.ScheduledJobProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

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
@JsonFilter("TrustedDevicesMultifactorProperties")
public class TrustedDevicesMultifactorProperties implements Serializable {

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
    private ScheduledJobProperties cleaner = new ScheduledJobProperties("PT15S", "PT2M");

    /**
     * Store devices records inside MongoDb.
     */
    @NestedConfigurationProperty
    private MongoDbTrustedDevicesMultifactorProperties mongo = new MongoDbTrustedDevicesMultifactorProperties();

    /**
     * Store devices records inside CouchDb.
     */
    @NestedConfigurationProperty
    private CouchDbTrustedDevicesMultifactorProperties couchDb = new CouchDbTrustedDevicesMultifactorProperties();

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
        crypto.getEncryption().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }
}
