package org.apereo.cas.otp.web.flow;

import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.AbstractMultifactorAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.action.EventFactorySupport;
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
    private final OneTimeTokenCredentialRepository repository;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val principal = resolvePrincipal(WebUtils.getAuthentication(requestContext).getPrincipal());
        val uid = principal.getId();

        val accounts = repository.get(uid);
        if (accounts == null || accounts.isEmpty()) {
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_REGISTER);
        }
        if (accounts.size() > 1) {
            WebUtils.putOneTimeTokenAccounts(requestContext, accounts);
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_CONFIRM);
        }
        WebUtils.putOneTimeTokenAccount(requestContext, accounts.iterator().next());
        return success();
    }
}
