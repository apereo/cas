package org.apereo.cas.gauth.web.flow;

import org.apereo.cas.authentication.device.MultifactorAuthenticationRegisteredDevice;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorAccount;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.actions.MultifactorAuthenticationDeviceProviderAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link GoogleAuthenticatorAuthenticationDeviceProviderAction}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
public class GoogleAuthenticatorAuthenticationDeviceProviderAction extends BaseCasWebflowAction
    implements MultifactorAuthenticationDeviceProviderAction {
    private final OneTimeTokenCredentialRepository repository;
    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Exception {
        val authentication = WebUtils.getAuthentication(requestContext);
        val principal = authentication.getPrincipal();

        val accounts = repository.get(principal.getId())
            .stream()
            .filter(Objects::nonNull)
            .map(GoogleAuthenticatorAccount.class::cast)
            .map(this::mapGoogleAuthenticatorAccount)
            .collect(Collectors.toList());
        WebUtils.putMultifactorAuthenticationRegisteredDevices(requestContext, accounts);
        return null;
    }

    protected MultifactorAuthenticationRegisteredDevice mapGoogleAuthenticatorAccount(final GoogleAuthenticatorAccount acct) {
        return MultifactorAuthenticationRegisteredDevice
            .builder()
            .id(String.valueOf(acct.getId()))
            .name(acct.getName())
            .lastUsedDateTime(acct.getLastUsedDateTime())
            .source("Google Authenticator")
            .payload(acct.toJson())
            .build();
    }
}
