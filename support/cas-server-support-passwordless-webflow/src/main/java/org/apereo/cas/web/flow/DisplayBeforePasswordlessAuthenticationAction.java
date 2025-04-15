package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessRequestParser;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.UnauthorizedServiceException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.Optional;

/**
 * This is {@link DisplayBeforePasswordlessAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class DisplayBeforePasswordlessAuthenticationAction extends BasePasswordlessCasWebflowAction {
    private final PasswordlessUserAccountStore passwordlessUserAccountStore;

    private final PasswordlessRequestParser passwordlessRequestParser;

    public DisplayBeforePasswordlessAuthenticationAction(final CasConfigurationProperties casProperties,
                                                         final PasswordlessUserAccountStore passwordlessUserAccountStore,
                                                         final PasswordlessRequestParser passwordlessRequestParser,
                                                         final MultifactorAuthenticationTriggerSelectionStrategy multifactorTriggerSelectionStrategy,
                                                         final PrincipalFactory passwordlessPrincipalFactory,
                                                         final AuthenticationSystemSupport authenticationSystemSupport) {
        super(casProperties, multifactorTriggerSelectionStrategy, passwordlessPrincipalFactory, authenticationSystemSupport);
        this.passwordlessUserAccountStore = passwordlessUserAccountStore;
        this.passwordlessRequestParser = passwordlessRequestParser;
    }

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val attributes = requestContext.getCurrentEvent().getAttributes();
        if (attributes.contains(CasWebflowConstants.TRANSITION_ID_ERROR)) {
            val error = attributes.get(CasWebflowConstants.TRANSITION_ID_ERROR, Exception.class);
            requestContext.getFlowScope().put(CasWebflowConstants.TRANSITION_ID_ERROR, error);
            val user = PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);
            PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(requestContext, user);
            return success();
        }
        val account = findPasswordlessUserAccount(requestContext).orElseThrow(
            () -> UnauthorizedServiceException.denied("Unable to locate passwordless user account"));
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(requestContext, account);
        return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_CREATE);
    }


    protected Optional<? extends PasswordlessUserAccount> findPasswordlessUserAccount(
        final RequestContext requestContext) throws Throwable {
        val username = requestContext.getRequestParameters().get(PasswordlessRequestParser.PARAMETER_USERNAME);
        if (StringUtils.isNotBlank(username)) {
            val passwordlessRequest = passwordlessRequestParser.parse(username);
            PasswordlessWebflowUtils.putPasswordlessAuthenticationRequest(requestContext, passwordlessRequest);
            return passwordlessUserAccountStore.findUser(passwordlessRequest);
        }
        val account = PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);
        if (account != null) {
            val passwordlessRequest = passwordlessRequestParser.parse(account.getUsername());
            PasswordlessWebflowUtils.putPasswordlessAuthenticationRequest(requestContext, passwordlessRequest);
            return Optional.of(account);
        }
        throw UnauthorizedServiceException.denied("Denied");
    }
}
