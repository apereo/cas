package org.apereo.cas.webauthn.registration;

import com.yubico.webauthn.attestation.Attestation;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link WebAuthnRegistrationResult}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
public class WebAuthnRegistrationResult {

    private PublicKeyCredentialDescriptor keyId;

    private boolean attestationTrusted;

    private ByteArray publicKeyCose;

    @Builder.Default
    private final List<String> warnings = new ArrayList<>();

    @Builder.Default
    private final Optional<Attestation> attestationMetadata = Optional.empty();
}
