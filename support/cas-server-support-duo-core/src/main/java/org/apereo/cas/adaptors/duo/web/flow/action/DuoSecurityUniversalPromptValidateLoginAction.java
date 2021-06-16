package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityUniversalPromptCredential;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderBean;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DuoSecurityUniversalPromptPrepareLoginAction}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class DuoSecurityUniversalPromptValidateLoginAction extends DuoSecurityAuthenticationWebflowAction {
    static final String REQUEST_PARAMETER_CODE = "duo_code";

    static final String REQUEST_PARAMETER_STATE = "state";

    private final CentralAuthenticationService centralAuthenticationService;

    private final MultifactorAuthenticationProviderBean<
        DuoSecurityMultifactorAuthenticationProvider, DuoSecurityMultifactorProperties> duoProviderBean;

    private final AuthenticationSystemSupport authenticationSystemSupport;

    public DuoSecurityUniversalPromptValidateLoginAction(final CasWebflowEventResolver duoAuthenticationWebflowEventResolver,
                                                         final CentralAuthenticationService centralAuthenticationService,
                                                         final MultifactorAuthenticationProviderBean duoProviderBean,
                                                         final AuthenticationSystemSupport authenticationSystemSupport) {
        super(duoAuthenticationWebflowEventResolver);
        this.centralAuthenticationService = centralAuthenticationService;
        this.duoProviderBean = duoProviderBean;
        this.authenticationSystemSupport = authenticationSystemSupport;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val requestParameters = requestContext.getRequestParameters();
        if (requestParameters.contains(REQUEST_PARAMETER_CODE) && requestParameters.contains(REQUEST_PARAMETER_STATE)) {
            val duoCode = requestParameters.get(REQUEST_PARAMETER_CODE, String.class);
            val duoState = requestParameters.get(REQUEST_PARAMETER_STATE, String.class);
            LOGGER.trace("Received Duo Security code [{}] and state [{}]", duoCode, duoState);

            try {
                val ticket = centralAuthenticationService.getTicket(duoState, TransientSessionTicket.class);
                val properties = ticket.getProperties();

                val duoSecurityIdentifier = (String) properties.get("duoProviderId");
                val authentication = (Authentication) properties.get("authentication");
                val registeredService = (RegisteredService) properties.get("registeredService");

                val credential = new DuoSecurityUniversalPromptCredential(duoCode, authentication);
                val provider = duoProviderBean.getProvider(duoSecurityIdentifier);
                credential.setProviderId(provider.createUniqueId());
                WebUtils.putCredential(requestContext, credential);

                val authenticationResultBuilder = (AuthenticationResultBuilder) properties.get("authenticationResultBuilder");
                WebUtils.putAuthenticationResultBuilder(authenticationResultBuilder, requestContext);
                val authenticationResult = authenticationResultBuilder.build(authenticationSystemSupport.getPrincipalElectionStrategy());
                WebUtils.putAuthenticationResult(authenticationResult, requestContext);
                WebUtils.putAuthentication(authenticationResult.getAuthentication(), requestContext);
                WebUtils.putRegisteredService(requestContext, registeredService);
                WebUtils.putServiceIntoFlowScope(requestContext, ticket.getService());
                return super.doExecute(requestContext);
            } catch (final Exception e) {
                LoggingUtils.warn(LOGGER, e);
                return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_ERROR);
            } finally {
                centralAuthenticationService.deleteTicket(duoState);
            }
        }
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_SKIP);
    }
}
