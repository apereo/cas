package org.apereo.cas.configuration.model.support.mfa.webauthn;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorProviderProperties;
import org.apereo.cas.configuration.model.support.mfa.u2f.U2FJpaMultifactorProperties;
import org.apereo.cas.configuration.model.support.quartz.ScheduledJobProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.concurrent.TimeUnit;

/**
 * This is {@link WebAuthnMultifactorProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-webauthn")
@Getter
@Setter
public class WebAuthnMultifactorProperties extends BaseMultifactorProviderProperties {

    /**
     * Provider id by default.
     */
    public static final String DEFAULT_IDENTIFIER = "mfa-webauthn";

    private static final long serialVersionUID = 4211350313777066398L;

    /**
     * Name of the principal attribute that indicates the principal's
     * display name, primarily used for device registration.
     */
    private String displayNameAttribute = "displayName";

    /**
     * Indicates whether this provider should support trusted devices.
     */
    private boolean trustedDeviceEnabled;

    /**
     * Trusted device metadata to contain trusted attestation root certificates
     * to pre-seed the metadata service.
     */
    @NestedConfigurationProperty
    private SpringResourceProperties trustedDeviceMetadata = new SpringResourceProperties();

    /**
     * The extension input to set for the {@code appid} extension when initiating authentication operations.
     * If this member is set, starting an assertion op will automatically set the
     * {@code appid} extension input, and finish assertion op will
     * adjust its verification logic to also accept this AppID as an alternative to the RP ID.
     * By default, this is not set.
     */
    @RequiredProperty
    private String applicationId;

    /**
     * The human-palatable name of the Relaying Party.
     */
    @RequiredProperty
    private String relyingPartyName;

    /**
     * The id that will be set as the rp parameter when initiating registration operations,
     * and which id hash will be compared against. This is a required parameter.
     * A successful registration or authentication operation requires rp id hash to exactly
     * equal the SHA-256 hash of this id member. Alternatively, it may
     * instead equal the SHA-256 hash of application id if the latter is present.
     */
    @RequiredProperty
    private String relyingPartyId;

    /**
     * Expire and forget device registration records after this period.
     */
    private long expireDevices = 30;

    /**
     * Device registration record expiration time unit.
     */
    private TimeUnit expireDevicesTimeUnit = TimeUnit.DAYS;
    
    /**
     * The allowed origins that returned authenticator responses will be compared against.
     * The default is set to the server name. A successful registration or authentication
     * operation requires origins to exactly equal one of these values.
     */
    private String allowedOrigins;

    /**
     * If {@code true} finish registration op and finish assertion will
     * accept responses containing extension outputs for
     * which there was no extension input.
     */
    private boolean allowUnrequestedExtensions;

    /**
     * If false finish registration op will only allow
     * registrations where the attestation signature can be linked to a trusted attestation root. This excludes self
     * attestation and none attestation. Regardless of the value of this option, invalid attestation
     * statements of supported formats will always be
     * rejected. For example, a "packed" attestation statement with an
     * invalid signature will be rejected even if this
     * option is set to true.
     */
    private boolean allowUntrustedAttestation;

    /**
     * If true, finish assertion op will fail if the  signature counter value in the response is not
     * strictly greater than the stored signature counter value.
     */
    private boolean validateSignatureCounter = true;

    /**
     * Accepted values are: {@code DIRECT}, {@code INDIRECT}, {@code NONE}.
     * The argument for the attestation parameter in
     * registration operations. Unless your application has a concrete policy
     * for authenticator attestation, it is recommended to leave this
     * parameter undefined.
     */
    private String attestationConveyancePreference = "DIRECT";

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
     * Store device registration records inside a JDBC resource.
     */
    @NestedConfigurationProperty
    private U2FJpaMultifactorProperties jpa = new U2FJpaMultifactorProperties();

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

    public WebAuthnMultifactorProperties() {
        setId(DEFAULT_IDENTIFIER);
        crypto.getEncryption().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }
}
