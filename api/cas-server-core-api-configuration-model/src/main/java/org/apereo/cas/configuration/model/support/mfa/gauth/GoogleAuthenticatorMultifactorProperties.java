package org.apereo.cas.configuration.model.support.mfa.gauth;

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
 * This is {@link GoogleAuthenticatorMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-gauth")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("GoogleAuthenticatorMultifactorProperties")
public class GoogleAuthenticatorMultifactorProperties extends BaseMultifactorAuthenticationProviderProperties {

    /**
     * Provider id by default.
     */
    public static final String DEFAULT_IDENTIFIER = "mfa-gauth";

    private static final long serialVersionUID = -7401748853833491119L;

    /**
     * Core/common settings for Google Multifactor authentication.
     */
    @NestedConfigurationProperty
    private CoreGoogleAuthenticatorMultifactorProperties core = new CoreGoogleAuthenticatorMultifactorProperties();

    /**
     * Store google authenticator devices inside a MongoDb instance.
     */
    @NestedConfigurationProperty
    private MongoDbGoogleAuthenticatorMultifactorProperties mongo = new MongoDbGoogleAuthenticatorMultifactorProperties();

    /**
     * Store google authenticator devices inside a DynamoDb instance.
     */
    @NestedConfigurationProperty
    private DynamoDbGoogleAuthenticatorMultifactorProperties dynamoDb = new DynamoDbGoogleAuthenticatorMultifactorProperties();

    /**
     * Store google authenticator devices inside a LDAP directories.
     */
    @NestedConfigurationProperty
    private LdapGoogleAuthenticatorMultifactorProperties ldap = new LdapGoogleAuthenticatorMultifactorProperties();

    /**
     * Store google authenticator devices inside a jdbc instance.
     */
    @NestedConfigurationProperty
    private JpaGoogleAuthenticatorMultifactorProperties jpa = new JpaGoogleAuthenticatorMultifactorProperties();

    /**
     * Store google authenticator devices inside a json file.
     */
    @NestedConfigurationProperty
    private JsonGoogleAuthenticatorMultifactorProperties json = new JsonGoogleAuthenticatorMultifactorProperties();

    /**
     * Store google authenticator devices via a rest interface.
     */
    @NestedConfigurationProperty
    private RestfulGoogleAuthenticatorMultifactorProperties rest = new RestfulGoogleAuthenticatorMultifactorProperties();

    /**
     * Store google authenticator devices via CouchDb.
     */
    @NestedConfigurationProperty
    private CouchDbGoogleAuthenticatorMultifactorProperties couchDb = new CouchDbGoogleAuthenticatorMultifactorProperties();

    /**
     * Store google authenticator devices via Redis.
     */
    @NestedConfigurationProperty
    private RedisGoogleAuthenticatorMultifactorProperties redis = new RedisGoogleAuthenticatorMultifactorProperties();

    /**
     * Crypto settings that sign/encrypt the records.
     */
    @NestedConfigurationProperty
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

    /**
     * Control how stale expired tokens should be cleared from the underlying store.
     */
    @NestedConfigurationProperty
    private ScheduledJobProperties cleaner = new ScheduledJobProperties("PT1M", "PT1M");

    public GoogleAuthenticatorMultifactorProperties() {
        setId(DEFAULT_IDENTIFIER);
        crypto.getEncryption().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }

}
