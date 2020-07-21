package org.apereo.cas.configuration.model.support.mfa.u2f;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorProviderProperties;
import org.apereo.cas.configuration.model.support.quartz.ScheduledJobProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.fasterxml.jackson.annotation.JsonFilter;
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
@JsonFilter("U2FMultifactorProperties")
public class U2FMultifactorProperties extends BaseMultifactorProviderProperties {

    /**
     * Provider id by default.
     */
    public static final String DEFAULT_IDENTIFIER = "mfa-u2f";

    private static final long serialVersionUID = 6151350313777066398L;

    /**
     * Store device registration records inside a JDBC resource.
     */
    @NestedConfigurationProperty
    private U2FJpaMultifactorProperties jpa = new U2FJpaMultifactorProperties();

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
    @NestedConfigurationProperty
    private U2FMongoDbMultifactorProperties mongo = new U2FMongoDbMultifactorProperties();

    /**
     * Store device registration records inside a redis resource.
     */
    @NestedConfigurationProperty
    private U2FRedisMultifactorProperties redis = new U2FRedisMultifactorProperties();

    /**
     * Store device registration records inside a dynamodb database resource.
     */
    @NestedConfigurationProperty
    private U2FDynamoDbMultifactorProperties dynamoDb = new U2FDynamoDbMultifactorProperties();

    /**
     * Store device registration records inside a static JSON resource.
     */
    @NestedConfigurationProperty
    private U2FJsonMultifactorProperties json = new U2FJsonMultifactorProperties();

    /**
     * Store device registration records via a Groovy script.
     */
    @NestedConfigurationProperty
    private U2FGroovyMultifactorProperties groovy = new U2FGroovyMultifactorProperties();

    /**
     * Store device registration records via REST APIs.
     */
    @NestedConfigurationProperty
    private U2FRestfulMultifactorProperties rest = new U2FRestfulMultifactorProperties();

    /**
     * Indicates whether this provider should support trusted devices.
     */
    private boolean trustedDeviceEnabled;

    /**
     * Store device registration records via CouchDb.
     */
    @NestedConfigurationProperty
    private U2FCouchDbMultifactorProperties couchDb = new U2FCouchDbMultifactorProperties();

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

}
