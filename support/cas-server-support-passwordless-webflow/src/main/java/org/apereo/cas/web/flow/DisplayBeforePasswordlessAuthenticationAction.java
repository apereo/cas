package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessRequestParser;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.UnauthorizedServiceException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

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
                                                         final PasswordlessRequestParser passwordlessRequestParser) {
        super(casProperties);
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
        val username = requestContext.getRequestParameters().get(PasswordlessRequestParser.PARAMETER_USERNAME);
        if (StringUtils.isBlank(username)) {
            throw UnauthorizedServiceException.denied("Denied");
        }
        val passwordlessRequest = passwordlessRequestParser.parse(username);
        val account = passwordlessUserAccountStore.findUser(passwordlessRequest);
        if (account.isEmpty()) {
            LOGGER.error("Unable to locate passwordless user account for [{}]", username);
            throw UnauthorizedServiceException.denied("Denied: %s".formatted(username));
        }
        val user = account.get();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(requestContext, user);
        PasswordlessWebflowUtils.putPasswordlessAuthenticationRequest(requestContext, passwordlessRequest);
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_CREATE);
    }

}
