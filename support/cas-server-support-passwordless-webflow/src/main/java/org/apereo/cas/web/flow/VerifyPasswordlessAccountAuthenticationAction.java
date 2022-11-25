package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link VerifyPasswordlessAccountAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class VerifyPasswordlessAccountAuthenticationAction extends BasePasswordlessCasWebflowAction {
    private final PasswordlessUserAccountStore passwordlessUserAccountStore;

    public VerifyPasswordlessAccountAuthenticationAction(final CasConfigurationProperties casProperties,
                                                         final PasswordlessUserAccountStore passwordlessUserAccountStore) {
        super(casProperties);
        this.passwordlessUserAccountStore = passwordlessUserAccountStore;
    }

    @Override
    public Event doExecute(final RequestContext requestContext) {
        val username = requestContext.getRequestParameters().getRequired("username");
        val account = passwordlessUserAccountStore.findUser(username);
        if (account.isEmpty()) {
            WebUtils.addErrorMessageToContext(requestContext, "passwordless.error.unknown.user");
            return error();
        }
        val user = account.get();
        if (user.isRequestPassword()) {
            WebUtils.putPasswordlessAuthenticationAccount(requestContext, user);
            val isDelegationActive = isDelegatedAuthenticationActiveFor(requestContext, user);
            DelegationWebflowUtils.putDelegatedAuthenticationDisabled(requestContext, !isDelegationActive);
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_PROMPT);
        }
        WebUtils.putPasswordlessAuthenticationAccount(requestContext, user);
        return success();
    }
}
