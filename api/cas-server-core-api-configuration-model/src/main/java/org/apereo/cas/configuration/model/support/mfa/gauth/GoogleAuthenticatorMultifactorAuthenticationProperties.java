package org.apereo.cas.configuration.model.support.mfa.gauth;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;
import org.apereo.cas.configuration.model.support.quartz.ScheduledJobProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link GoogleAuthenticatorMultifactorAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-gauth")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("GoogleAuthenticatorMultifactorProperties")
public class GoogleAuthenticatorMultifactorAuthenticationProperties extends BaseMultifactorAuthenticationProviderProperties {

    /**
     * Provider id by default.
     */
    public static final String DEFAULT_IDENTIFIER = "mfa-gauth";

    private static final long serialVersionUID = -7401748853833491119L;

    /**
     * Issuer used in the barcode when dealing with device registration events.
     * Used in the registration URL to identify CAS.
     */
    @RequiredProperty
    private String issuer = "CASIssuer";

    /**
     * Label used in the barcode when dealing with device registration events.
     * Used in the registration URL to identify CAS.
     */
    @RequiredProperty
    private String label = "CASLabel";

    /**
     * Length of the generated code.
     */
    private int codeDigits = 6;

    /**
     * The expiration time of the generated code in seconds.
     */
    private long timeStepSize = 30;

    /**
     * Since TOTP passwords are time-based, it is essential that the clock of both the server and
     * the client are synchronised within
     * the tolerance defined here as the window size.
     */
    private int windowSize = 3;

    /**
     * When enabled, allows the user/system to accept multiple accounts
     * and device registrations per user, allowing one to switch between
     * or register new devices/accounts automatically.
     */
    private boolean multipleDeviceRegistrationEnabled;
    
    /**
     * Store google authenticator devices inside a MongoDb instance.
     */
    @NestedConfigurationProperty
    private MongoDbGoogleAuthenticatorMultifactorProperties mongo = new MongoDbGoogleAuthenticatorMultifactorProperties();

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
     * Indicates whether this provider should support trusted devices.
     */
    private boolean trustedDeviceEnabled;

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

    public GoogleAuthenticatorMultifactorAuthenticationProperties() {
        setId(DEFAULT_IDENTIFIER);
        crypto.getEncryption().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }

}
