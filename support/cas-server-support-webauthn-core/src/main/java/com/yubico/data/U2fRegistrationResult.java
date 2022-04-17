package com.yubico.data;

import com.yubico.webauthn.attestation.Attestation;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Value
@Builder
public class U2fRegistrationResult {

    @NonNull
    private final PublicKeyCredentialDescriptor keyId;

    private final boolean attestationTrusted;

    @NonNull
    private final ByteArray publicKeyCose;

    @NonNull
    @Builder.Default
    private final List<String> warnings = Collections.emptyList();

    @NonNull
    @Builder.Default
    private final Optional<Attestation> attestationMetadata = Optional.empty();
}
