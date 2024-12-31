package org.apereo.cas.configuration.model.support.mfa.webauthn;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.ClassPathResource;

import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link WebAuthnMultifactorAuthenticationCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-webauthn")
@Getter
@Setter
@Accessors(chain = true)

public class WebAuthnMultifactorAuthenticationCoreProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -919073482703977440L;

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
    private WebAuthnMultifactorAttestationTrustSourceProperties trustSource = new WebAuthnMultifactorAttestationTrustSourceProperties();

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
     * Whether WebAuthn functionality
     * should be activated and enabled.
     */
    private boolean enabled = true;

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
     * Configure the authentication flow to allow
     * web-authn to be used as the first primary factor
     * for authentication. Registered accounts with a valid
     * webauthn registration record can choose to login
     * using their device as the first step.
     */
    private boolean allowPrimaryAuthentication;

    /**
     * When enabled, allows the user/system to accept multiple accounts
     * and device registrations per user, allowing one to switch between
     * or register new devices/accounts automatically.
     */
    private boolean multipleDeviceRegistrationEnabled;

    /**
     * When enabled, allows the user to a scan a QR code on an external device
     * and authenticate later on the primary device. This is useful in scenarios
     * where the primary authentication device is not registered with CAS
     * and the user has a secondary device that is registered and does not wish
     * to use the primary device to authenticate for security or other practical reasons.
     */
    private boolean qrCodeAuthenticationEnabled;
    
    public WebAuthnMultifactorAuthenticationCoreProperties() {
        trustSource.getTrustedDeviceMetadata().setLocation(new ClassPathResource("webauthn-metadata.json"));
    }
}
