package org.apereo.cas.webauthn.web.flow.account;

import org.apereo.cas.authentication.device.MultifactorAuthenticationRegisteredDevice;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.actions.MultifactorAuthenticationDeviceProviderAction;
import org.apereo.cas.web.support.WebUtils;

import com.yubico.core.RegistrationStorage;
import com.yubico.data.CredentialRegistration;
import com.yubico.internal.util.JacksonCodecs;
import com.yubico.webauthn.attestation.Attestation;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link WebAuthnMultifactorDeviceProviderAction}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
public class WebAuthnMultifactorDeviceProviderAction extends BaseCasWebflowAction
    implements MultifactorAuthenticationDeviceProviderAction {

    private final RegistrationStorage webAuthnCredentialRepository;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val authentication = WebUtils.getAuthentication(requestContext);
        val principal = authentication.getPrincipal();

        val registrations = webAuthnCredentialRepository.getRegistrationsByUsername(principal.getId());
        val accounts = registrations
            .stream()
            .filter(Objects::nonNull)
            .map(this::mapWebAuthnAccount)
            .collect(Collectors.toList());
        WebUtils.putMultifactorAuthenticationRegisteredDevices(requestContext, accounts);
        return null;
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
