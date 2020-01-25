package com.yubico.webauthn.data;

import com.yubico.webauthn.attestation.Attestation;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

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
