package org.apereo.cas.configuration.model.support.mfa.u2f;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;
import org.apereo.cas.configuration.model.support.quartz.ScheduledJobProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link U2FMultifactorAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-u2f")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("U2FMultifactorAuthenticationProperties")
public class U2FMultifactorAuthenticationProperties extends BaseMultifactorAuthenticationProviderProperties {

    /**
     * Provider id by default.
     */
    public static final String DEFAULT_IDENTIFIER = "mfa-u2f";

    private static final long serialVersionUID = 6151350313777066398L;

    /**
     * Core/common U2F settings.
     */
    @NestedConfigurationProperty
    private U2FCoreMultifactorAuthenticationProperties core = new U2FCoreMultifactorAuthenticationProperties();

    /**
     * Store device registration records inside a JDBC resource.
     */
    @NestedConfigurationProperty
    private U2FJpaMultifactorAuthenticationProperties jpa = new U2FJpaMultifactorAuthenticationProperties();

    /**
     * Store device registration records inside a MongoDb resource.
     */
    @NestedConfigurationProperty
    private U2FMongoDbMultifactorAuthenticationProperties mongo = new U2FMongoDbMultifactorAuthenticationProperties();

    /**
     * Store device registration records inside a redis resource.
     */
    @NestedConfigurationProperty
    private U2FRedisMultifactorAuthenticationProperties redis = new U2FRedisMultifactorAuthenticationProperties();

    /**
     * Store device registration records inside a dynamodb database resource.
     */
    @NestedConfigurationProperty
    private U2FDynamoDbMultifactorAuthenticationProperties dynamoDb = new U2FDynamoDbMultifactorAuthenticationProperties();

    /**
     * Store device registration records inside a static JSON resource.
     */
    @NestedConfigurationProperty
    private U2FJsonMultifactorAuthenticationProperties json = new U2FJsonMultifactorAuthenticationProperties();

    /**
     * Store device registration records via a Groovy script.
     */
    @NestedConfigurationProperty
    private U2FGroovyMultifactorAuthenticationProperties groovy = new U2FGroovyMultifactorAuthenticationProperties();

    /**
     * Store device registration records via REST APIs.
     */
    @NestedConfigurationProperty
    private U2FRestfulMultifactorAuthenticationProperties rest = new U2FRestfulMultifactorAuthenticationProperties();

    /**
     * Store device registration records via CouchDb.
     */
    @NestedConfigurationProperty
    private U2FCouchDbMultifactorAuthenticationProperties couchDb = new U2FCouchDbMultifactorAuthenticationProperties();

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

    public U2FMultifactorAuthenticationProperties() {
        crypto.getEncryption().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
        setId(DEFAULT_IDENTIFIER);
    }

}
