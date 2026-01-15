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
    
    public String getUsername() {
        return userIdentity.getName();
    }
}
