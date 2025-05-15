package org.apereo.cas.webauthn;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.device.MultifactorAuthenticationDeviceManager;
import org.apereo.cas.authentication.device.MultifactorAuthenticationRegisteredDevice;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.function.FunctionUtils;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.yubico.core.RegistrationStorage;
import com.yubico.data.CredentialRegistration;
import com.yubico.internal.util.JacksonCodecs;
import com.yubico.webauthn.attestation.Attestation;
import com.yubico.webauthn.data.ByteArray;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
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
    private static final ObjectWriter OBJECT_WRITER = JacksonCodecs.json().writerWithDefaultPrettyPrinter();

    private final RegistrationStorage webAuthnCredentialRepository;
    private final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider;
    
    @Override
    public List<MultifactorAuthenticationRegisteredDevice> findRegisteredDevices(final Principal principal) {
        val registrations = webAuthnCredentialRepository.getRegistrationsByUsername(principal.getId());
        return registrations
            .stream()
            .filter(Objects::nonNull)
            .map(this::mapWebAuthnAccount)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public void removeRegisteredDevice(final Principal principal, final String deviceId) {
        FunctionUtils.doAndHandle(__ -> {
            val credentialId = ByteArray.fromBase64Url(deviceId);
            webAuthnCredentialRepository.removeRegistrationByUsernameAndCredentialId(principal.getId(), credentialId);
        });
    }

    @Override
    public List<String> getSource() {
        return List.of("Web Authn");
    }

    protected MultifactorAuthenticationRegisteredDevice mapWebAuthnAccount(final CredentialRegistration acct) {
        val attestation = Optional.ofNullable(acct.getAttestationMetadata()).orElseGet(Attestation::empty);
        val vendor = attestation.getVendorProperties().orElseGet(Map::of);
        val device = attestation.getDeviceProperties().orElseGet(Map::of);
        return FunctionUtils.doUnchecked(() -> MultifactorAuthenticationRegisteredDevice
            .builder()
            .id(acct.getCredential().getCredentialId().getBase64Url())
            .name(acct.getCredentialNickname())
            .type(vendor.get("name"))
            .model(device.get("displayName"))
            .lastUsedDateTime(acct.getRegistrationTime().toString())
            .payload(OBJECT_WRITER.writeValueAsString(acct))
            .source(getSource().getFirst())
            .details(Map.of("providerId", multifactorAuthenticationProvider.getObject().getId()))
            .build());
    }
}
