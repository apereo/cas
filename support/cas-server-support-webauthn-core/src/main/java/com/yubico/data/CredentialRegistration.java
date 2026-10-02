package com.yubico.data;
import module java.base;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.attestation.Attestation;
import com.yubico.webauthn.data.AuthenticatorTransport;
import com.yubico.webauthn.data.UserIdentity;
import lombok.Builder;
import lombok.Value;
import lombok.With;

@Value
@Builder
@With
public class CredentialRegistration {

    UserIdentity userIdentity;

    String credentialNickname;

    SortedSet<AuthenticatorTransport> transports;

    Instant registrationTime;

    RegisteredCredential credential;

    Attestation attestationMetadata;

    /**
     * Whether the authenticator reported, through the {@code credProps} extension, that it created a
     * discoverable credential; {@code null} when the client did not report it.
     */
    Boolean discoverable;

    /**
     * AAGUID of the authenticator or passkey provider that created the credential, as a lowercase UUID string;
     * {@code null} when the authenticator did not identify itself.
     */
    String aaguid;

    /**
     * Name given to the authenticator for the WebAuthn user entity when it differs from the principal id, such as the
     * username typed at login for a passkey upgrade, so that the password manager can keep showing that name;
     * {@code null} when the authenticator was given the principal id.
     */
    String userEntityName;

    public String getUsername() {
        return userIdentity.getName();
    }
}
