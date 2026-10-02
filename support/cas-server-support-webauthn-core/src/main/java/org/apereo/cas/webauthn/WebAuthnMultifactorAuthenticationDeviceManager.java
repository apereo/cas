package org.apereo.cas.webauthn;

import module java.base;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.device.MultifactorAuthenticationDeviceManager;
import org.apereo.cas.authentication.device.MultifactorAuthenticationRegisteredDevice;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.webauthn.web.WebAuthnController;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.yubico.core.RegistrationStorage;
import com.yubico.data.CredentialRegistration;
import com.yubico.webauthn.attestation.Attestation;
import com.yubico.webauthn.data.ByteArray;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link WebAuthnMultifactorAuthenticationDeviceManager}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
public class WebAuthnMultifactorAuthenticationDeviceManager implements MultifactorAuthenticationDeviceManager {
    private static final ObjectWriter OBJECT_WRITER = WebAuthnUtils.getObjectMapper().writer();

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
        FunctionUtils.doAndHandle(_ -> {
            val credentialId = ByteArray.fromBase64Url(deviceId);
            webAuthnCredentialRepository.removeRegistrationByUsernameAndCredentialId(principal.getId(), credentialId);
        });
    }

    @Override
    public List<String> getSource() {
        return List.of("Web Authn");
    }

    /**
     * Map a registration to a registered device. When attestation metadata names no device, the passkey provider known
     * by the registration's AAGUID gives the model. A provider with an icon also adds the {@code icon} detail, the path
     * of the icon endpoint rather than the image itself, since registered devices are kept in the flow state.
     *
     * @param acct the registration
     * @return the registered device
     */
    protected MultifactorAuthenticationRegisteredDevice mapWebAuthnAccount(final CredentialRegistration acct) {
        val attestation = Optional.ofNullable(acct.getAttestationMetadata()).orElseGet(Attestation::empty);
        val vendor = attestation.getVendorProperties().orElseGet(Map::of);
        val device = attestation.getDeviceProperties().orElseGet(Map::of);
        val passkeyProvider = WebAuthnUtils.getPasskeyProvider(acct.getAaguid());
        val details = new LinkedHashMap<String, Object>();
        details.put("providerId", multifactorAuthenticationProvider.getObject().getId());
        passkeyProvider.filter(provider -> provider.icon() != null)
            .ifPresent(provider -> details.put("icon", WebAuthnController.getPasskeyProviderIconPath(acct.getAaguid())));
        return FunctionUtils.doUnchecked(() -> MultifactorAuthenticationRegisteredDevice
            .builder()
            .id(acct.getCredential().getCredentialId().getBase64Url())
            .name(acct.getCredentialNickname())
            .type(vendor.get("name"))
            .model(Optional.ofNullable(device.get("displayName"))
                .or(() -> passkeyProvider.map(WebAuthnUtils.PasskeyProvider::name))
                .orElse(null))
            .lastUsedDateTime(acct.getRegistrationTime().toString())
            .payload(OBJECT_WRITER.writeValueAsString(acct))
            .source(getSource().getFirst())
            .details(details)
            .build());
    }
}
