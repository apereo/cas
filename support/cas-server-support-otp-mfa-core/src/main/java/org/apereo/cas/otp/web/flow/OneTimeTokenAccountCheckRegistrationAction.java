package org.apereo.cas.otp.web.flow;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link OneTimeTokenAccountCheckRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
public class OneTimeTokenAccountCheckRegistrationAction extends AbstractMultifactorAuthenticationAction {
    protected final OneTimeTokenCredentialRepository repository;
    protected final CasConfigurationProperties casProperties;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val principal = resolvePrincipal(WebUtils.getAuthentication(requestContext).getPrincipal(), requestContext);
        val uid = principal.getId();

        val accounts = repository.get(uid);
        if (accounts == null || accounts.isEmpty()) {
            return routeToRegistration(requestContext, principal);
        }
        if (accounts.size() > 1) {
            MultifactorAuthenticationWebflowUtils.putOneTimeTokenAccounts(requestContext, accounts);
            return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_CONFIRM);
        }
        MultifactorAuthenticationWebflowUtils.putOneTimeTokenAccount(requestContext, accounts.iterator().next());
        return success();
    }

    protected Event routeToRegistration(final RequestContext requestContext, final Principal principal) {
        return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_REGISTER);
    }
}
