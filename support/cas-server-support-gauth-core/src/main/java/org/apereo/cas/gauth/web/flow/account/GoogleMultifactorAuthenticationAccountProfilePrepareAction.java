package org.apereo.cas.gauth.web.flow.account;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;

/**
 * This is {@link GoogleMultifactorAuthenticationAccountProfilePrepareAction}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class GoogleMultifactorAuthenticationAccountProfilePrepareAction extends ConsumerExecutionAction {
    public GoogleMultifactorAuthenticationAccountProfilePrepareAction(
        final OneTimeTokenCredentialRepository repository,
        final MultifactorAuthenticationProvider googleAuthenticatorMultifactorAuthenticationProvider,
        final CasConfigurationProperties casProperties) {
        super(requestContext -> {
            val principal = WebUtils.getAuthentication(requestContext).getPrincipal();
            val core = casProperties.getAuthn().getMfa().getGauth().getCore();
            val enabled = (core.isMultipleDeviceRegistrationEnabled() || repository.count(principal.getId()) == 0)
                && MultifactorAuthenticationWebflowUtils.isMultifactorDeviceRegistrationEnabled(requestContext);
            requestContext.getFlowScope().put("gauthAccountProfileRegistrationEnabled", enabled);
            MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(requestContext, googleAuthenticatorMultifactorAuthenticationProvider);
        });
    }
}

