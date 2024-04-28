package org.apereo.cas.webauthn;

import org.apereo.cas.authentication.device.MultifactorAuthenticationDeviceManager;
import org.apereo.cas.authentication.device.MultifactorAuthenticationRegisteredDevice;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.function.FunctionUtils;
import com.yubico.core.RegistrationStorage;
import com.yubico.data.CredentialRegistration;
import com.yubico.internal.util.JacksonCodecs;
import com.yubico.webauthn.attestation.Attestation;
import lombok.RequiredArgsConstructor;
import lombok.val;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link WebAuthnMultifactorAuthenticationDeviceManager}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
public class WebAuthnMultifactorAuthenticationDeviceManager implements MultifactorAuthenticationDeviceManager {
    private final RegistrationStorage webAuthnCredentialRepository;

    @Override
    public List<MultifactorAuthenticationRegisteredDevice> findRegisteredDevices(final Principal principal) {
        val registrations = webAuthnCredentialRepository.getRegistrationsByUsername(principal.getId());
        return registrations
            .stream()
            .filter(Objects::nonNull)
            .map(this::mapWebAuthnAccount)
            .collect(Collectors.toList());
    }

    protected MultifactorAuthenticationRegisteredDevice mapWebAuthnAccount(
        final CredentialRegistration acct) {
        val vendor = Optional.ofNullable(acct.getAttestationMetadata()).orElseGet(Attestation::empty).getVendorProperties().orElseGet(Map::of);
        val device = Optional.ofNullable(acct.getAttestationMetadata()).orElseGet(Attestation::empty).getDeviceProperties().orElseGet(Map::of);
        return FunctionUtils.doUnchecked(() -> MultifactorAuthenticationRegisteredDevice.builder()
            .id(acct.getCredential().getCredentialId().getBase64Url())
            .name(acct.getCredentialNickname())
            .type(vendor.get("name"))
            .model(device.get("displayName"))
            .lastUsedDateTime(acct.getRegistrationTime().toString())
            .payload(JacksonCodecs.json().writerWithDefaultPrettyPrinter().writeValueAsString(acct))
            .source("Web Authn")
            .build());
    }
}
