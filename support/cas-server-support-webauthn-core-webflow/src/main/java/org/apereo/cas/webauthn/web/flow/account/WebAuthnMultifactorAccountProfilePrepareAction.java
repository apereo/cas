package org.apereo.cas.webauthn.web.flow.account;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import com.yubico.core.RegistrationStorage;
import lombok.val;

/**
 * This is {@link WebAuthnMultifactorAccountProfilePrepareAction}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class WebAuthnMultifactorAccountProfilePrepareAction extends ConsumerExecutionAction {
    public WebAuthnMultifactorAccountProfilePrepareAction(
        final RegistrationStorage webAuthnCredentialRepository,
        final MultifactorAuthenticationProvider webAuthnMultifactorAuthenticationProvider,
        final CasConfigurationProperties casProperties) {
        super(requestContext -> {
            val principal = WebUtils.getAuthentication(requestContext).getPrincipal();
            val core = casProperties.getAuthn().getMfa().getWebAuthn().getCore();
            val enabled = core.isMultipleDeviceRegistrationEnabled() || webAuthnCredentialRepository.getRegistrationsByUsername(principal.getId()).isEmpty();
            requestContext.getFlowScope().put("webauthnAccountProfileRegistrationEnabled", enabled);
            MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(requestContext, webAuthnMultifactorAuthenticationProvider);
        });
    }
}

