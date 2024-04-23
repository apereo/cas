package org.apereo.cas.gauth.web.flow;


import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link GoogleAuthenticatorPrepareLoginAction}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
public class GoogleAuthenticatorPrepareLoginAction extends AbstractMultifactorAuthenticationAction {
    protected final CasConfigurationProperties casProperties;
    protected final OneTimeTokenCredentialRepository repository;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val principal = resolvePrincipal(WebUtils.getAuthentication(requestContext).getPrincipal(), requestContext);
        val enabled = casProperties.getAuthn().getMfa().getGauth().getCore().isMultipleDeviceRegistrationEnabled()
            && repository.count(principal.getId()) >= 1;
        WebUtils.putGoogleAuthenticatorMultipleDeviceRegistrationEnabled(requestContext, enabled);
        WebUtils.putOneTimeTokenAccounts(requestContext, repository.get(principal.getId()));
        return null;
    }
}
