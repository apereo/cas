package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessRequestParser;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
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
    private final PasswordlessRequestParser passwordlessRequestParser;

    public VerifyPasswordlessAccountAuthenticationAction(final CasConfigurationProperties casProperties,
                                                         final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy,
                                                         final PrincipalFactory passwordlessPrincipalFactory,
                                                         final AuthenticationSystemSupport authenticationSystemSupport,
                                                         final PasswordlessUserAccountStore passwordlessUserAccountStore,
                                                         final PasswordlessRequestParser passwordlessRequestParser) {
        super(casProperties, multifactorTriggerSelectionStrategy, passwordlessPrincipalFactory, authenticationSystemSupport);
        this.passwordlessUserAccountStore = passwordlessUserAccountStore;
        this.passwordlessRequestParser = passwordlessRequestParser;
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val username = requestContext.getRequestParameters().getRequired(PasswordlessRequestParser.PARAMETER_USERNAME);
        val passwordlessRequest = passwordlessRequestParser.parse(username);
        val account = passwordlessUserAccountStore.findUser(passwordlessRequest);
        if (account.isEmpty()) {
            WebUtils.addErrorMessageToContext(requestContext, "passwordless.error.unknown.user");
            return error();
        }
        val user = account.get();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(requestContext, user);
        PasswordlessWebflowUtils.putPasswordlessAuthenticationRequest(requestContext, passwordlessRequest);
        val isDelegationActive = isDelegatedAuthenticationActiveFor(requestContext, user);
        DelegationWebflowUtils.putDelegatedAuthenticationDisabled(requestContext, !isDelegationActive);

        if (user.isAllowSelectionMenu()) {
            return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_SELECT);
        }
        
        val requestPassword = doesPasswordlessAccountRequestPassword(user);
        WebUtils.putCasLoginFormViewable(requestContext, requestPassword);
        return requestPassword || user.getAllowedDelegatedClients().size() > 1
            ? eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_PROMPT)
            : success();
    }
}
