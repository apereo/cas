package org.apereo.cas.configuration.model.support.mfa.webauthn;

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
 * This is {@link WebAuthnMultifactorAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-webauthn")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("WebAuthnMultifactorProperties")
public class WebAuthnMultifactorAuthenticationProperties extends BaseMultifactorAuthenticationProviderProperties {

    /**
     * Provider id by default.
     */
    public static final String DEFAULT_IDENTIFIER = "mfa-webauthn";

    private static final long serialVersionUID = 4211350313777066398L;

    /**
     * WebAuthn core settings.
     */
    @NestedConfigurationProperty
    private WebAuthnMultifactorAuthenticationCoreProperties core = new WebAuthnMultifactorAuthenticationCoreProperties();

    /**
     * Store device registration records inside a static JSON resource.
     */
    @NestedConfigurationProperty
    private WebAuthnJsonMultifactorProperties json = new WebAuthnJsonMultifactorProperties();

    /**
     * Keep device registration records inside a MongoDb resource.
     */
    @NestedConfigurationProperty
    private WebAuthnMongoDbMultifactorProperties mongo = new WebAuthnMongoDbMultifactorProperties();

    /**
     * Store device registration records inside a redis resource.
     */
    @NestedConfigurationProperty
    private WebAuthnRedisMultifactorProperties redis = new WebAuthnRedisMultifactorProperties();

    /**
     * Store device registration records inside a dynamodb resource.
     */
    @NestedConfigurationProperty
    private WebAuthnDynamoDbMultifactorProperties dynamoDb = new WebAuthnDynamoDbMultifactorProperties();

    /**
     * Store device registration records inside an LDAP directory..
     */
    @NestedConfigurationProperty
    private WebAuthnLdapMultifactorProperties ldap = new WebAuthnLdapMultifactorProperties();

    /**
     * Store device registration records inside a JDBC resource.
     */
    @NestedConfigurationProperty
    private WebAuthnJpaMultifactorProperties jpa = new WebAuthnJpaMultifactorProperties();

    /**
     * Store device registration records via external REST APIs.
     */
    @NestedConfigurationProperty
    private WebAuthnRestfulMultifactorProperties rest = new WebAuthnRestfulMultifactorProperties();

    /**
     * Clean up expired records via a background cleaner process.
     */
    @NestedConfigurationProperty
    private ScheduledJobProperties cleaner = new ScheduledJobProperties("PT10S", "PT1M");

    /**
     * Properties and settings related to device registration records and encryption.
     */
    @NestedConfigurationProperty
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

    public WebAuthnMultifactorAuthenticationProperties() {
        setId(DEFAULT_IDENTIFIER);
        crypto.getEncryption().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }
}
