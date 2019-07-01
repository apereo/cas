package org.apereo.cas.configuration.model.support.mfa;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.SpringResourceProperties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

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
    private boolean allowUntrustedAttestation = true;

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

    public WebAuthnMultifactorProperties() {
        setId(DEFAULT_IDENTIFIER);
    }
}
