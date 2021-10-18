package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityUniversalPromptCredential;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderBean;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;

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
        DuoSecurityMultifactorAuthenticationProvider, DuoSecurityMultifactorAuthenticationProperties> duoProviderBean;

    private final AuthenticationSystemSupport authenticationSystemSupport;

    public DuoSecurityUniversalPromptValidateLoginAction(
        final CasWebflowEventResolver duoAuthenticationWebflowEventResolver,
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
            val duoState = requestParameters.get(REQUEST_PARAMETER_STATE, String.class);
            LOGGER.trace("Received Duo Security state [{}]", duoState);
            var ticket = (TransientSessionTicket) null;
            try {
                ticket = centralAuthenticationService.getTicket(duoState, TransientSessionTicket.class);
                val authentication = ticket.getProperty(Authentication.class.getSimpleName(), Authentication.class);
                populateContextWithCredential(requestContext, ticket, authentication);
                populateContextWithAuthentication(requestContext, ticket);
                populateContextWithService(requestContext, ticket);
                return super.doExecute(requestContext);
            } catch (final Exception e) {
                LoggingUtils.warn(LOGGER, e);
                return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_ERROR);
            } finally {
                if (ticket != null) {
                    val flowScope = ticket.getProperty(MutableAttributeMap.class.getSimpleName(), Map.class);
                    flowScope.forEach((key, value) -> requestContext.getFlowScope().put(key.toString(), value));
                    val credential = ticket.getProperty(Credential.class.getSimpleName(), Credential.class);
                    WebUtils.putCredential(requestContext, credential);
                }
                centralAuthenticationService.deleteTicket(duoState);
            }
        }
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_SKIP);
    }

    /**
     * Populate context with service.
     *
     * @param requestContext the request context
     * @param ticket         the ticket
     */
    protected void populateContextWithService(final RequestContext requestContext, final TransientSessionTicket ticket) {
        val registeredService = ticket.getProperty(RegisteredService.class.getSimpleName(), RegisteredService.class);
        WebUtils.putRegisteredService(requestContext, registeredService);
        WebUtils.putServiceIntoFlowScope(requestContext, ticket.getService());
    }

    /**
     * Populate context with credential.
     *
     * @param requestContext the request context
     * @param ticket         the ticket
     * @param authentication the authentication
     */
    protected void populateContextWithCredential(final RequestContext requestContext, final TransientSessionTicket ticket,
                                                 final Authentication authentication) {
        val requestParameters = requestContext.getRequestParameters();
        val duoCode = requestParameters.get(REQUEST_PARAMETER_CODE, String.class);
        LOGGER.trace("Received Duo Security code [{}]", duoCode);

        val duoSecurityIdentifier = ticket.getProperty("duoProviderId", String.class);
        val credential = new DuoSecurityUniversalPromptCredential(duoCode, authentication);
        val provider = duoProviderBean.getProvider(duoSecurityIdentifier);
        credential.setProviderId(provider.getId());
        WebUtils.putCredential(requestContext, credential);
    }

    /**
     * Populate context with authentication.
     *
     * @param requestContext the request context
     * @param ticket         the ticket
     */
    protected void populateContextWithAuthentication(final RequestContext requestContext, final TransientSessionTicket ticket) {
        val authenticationResultBuilder = ticket.getProperty(AuthenticationResultBuilder.class.getSimpleName(),
            AuthenticationResultBuilder.class);
        WebUtils.putAuthenticationResultBuilder(authenticationResultBuilder, requestContext);
        val authenticationResult = authenticationResultBuilder.build(authenticationSystemSupport.getPrincipalElectionStrategy());
        WebUtils.putAuthenticationResult(authenticationResult, requestContext);
        WebUtils.putAuthentication(authenticationResult.getAuthentication(), requestContext);
    }
}
