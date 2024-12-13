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
import org.springframework.webflow.action.EventFactorySupport;
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

        val account = findPasswordlessUserAccount(requestContext);
        if (account.isEmpty()) {
            LOGGER.error("Unable to locate passwordless user account");
            throw UnauthorizedServiceException.denied("Denied passwordless account access");
        }
        val user = account.get();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(requestContext, user);

        val passwordlessRequest = passwordlessRequestParser.parse(user.getUsername());
        PasswordlessWebflowUtils.putPasswordlessAuthenticationRequest(requestContext, passwordlessRequest);
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_CREATE);
    }


    protected Optional<? extends PasswordlessUserAccount> findPasswordlessUserAccount(final RequestContext requestContext) throws Throwable {
        val account = PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(requestContext, PasswordlessUserAccount.class);
        if (account != null) {
            return Optional.of(account);
        }
        val username = requestContext.getRequestParameters().get(PasswordlessRequestParser.PARAMETER_USERNAME);
        if (StringUtils.isBlank(username)) {
            throw UnauthorizedServiceException.denied("Denied");
        }
        val passwordlessRequest = passwordlessRequestParser.parse(username);
        return passwordlessUserAccountStore.findUser(passwordlessRequest);
    }
}
