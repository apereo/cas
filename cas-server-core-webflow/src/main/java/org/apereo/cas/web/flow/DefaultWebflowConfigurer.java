package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.RememberMeUsernamePasswordCredential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.services.UnauthorizedSsoServiceException;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.BinderConfiguration;
import org.springframework.webflow.engine.support.TransitionExecutingFlowExecutionExceptionHandler;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;

/**
 * This is {@link DefaultWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultWebflowConfigurer extends AbstractCasWebflowConfigurer {
    
    
    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();

        createDefaultGlobalExceptionHandlers(flow);
        createDefaultEndStates(flow);
        createDefaultDecisionStates(flow);
        createDefaultActionStates(flow);
        
        createRememberMeAuthnWebflowConfig(flow);
    }

    private void createRememberMeAuthnWebflowConfig(final Flow flow) {
        if (casProperties.getTicket().getTgt().getRememberMe().isEnabled()) {
            createFlowVariable(flow, "credential", RememberMeUsernamePasswordCredential.class);
            final ViewState state = (ViewState) flow.getState(CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
            final BinderConfiguration cfg = getViewStateBinderConfiguration(state);
            cfg.addBinding(new BinderConfiguration.Binding("rememberMe", null, false));
        } else {
            createFlowVariable(flow, "credential", UsernamePasswordCredential.class);
        }
    }

    /**
     * Create default action states.
     *
     * @param flow the flow
     */
    protected void createDefaultActionStates(final Flow flow) {
        final ActionState terminateSession = createActionState(flow, "terminateSession", 
                createEvaluateAction("terminateSessionAction"));
        createStateDefaultTransition(terminateSession, "gatewayRequestCheck");

        final ActionState serviceAuthorizationCheck = createActionState(flow, "serviceAuthorizationCheck", 
                createEvaluateAction("serviceAuthorizationCheck"));
        createStateDefaultTransition(serviceAuthorizationCheck, "initializeLoginForm");
    }

    /**
     * Create default global exception handlers.
     *
     * @param flow the flow
     */
    protected void createDefaultEndStates(final Flow flow) {
        createEndState(flow, "viewRedirectToUnauthorizedUrlView", "flowScope.unauthorizedRedirectUrl", true);
        createEndState(flow, "redirectView", "requestScope.response.url", true);
        createEndState(flow, "viewServiceErrorView", "casServiceErrorView");
        
        final EndState state = createEndState(flow, "viewGenericLoginSuccess", "casGenericSuccessView");
        state.getEntryActionList().add(createEvaluateAction("genericSuccessViewAction"));

        final EndState stateWarning = createEndState(flow, "showWarningView", "casConfirmView");
        stateWarning.getEntryActionList().add(createEvaluateAction("serviceWarningAction"));
    }

    /**
     * Create default global exception handlers.
     *
     * @param flow the flow
     */
    protected void createDefaultGlobalExceptionHandlers(final Flow flow) {
        final TransitionExecutingFlowExecutionExceptionHandler h = new TransitionExecutingFlowExecutionExceptionHandler();
        h.add(UnauthorizedSsoServiceException.class, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
        h.add(NoSuchFlowExecutionException.class, "viewServiceErrorView");
        h.add(UnauthorizedServiceException.class, "serviceUnauthorizedCheck");
        flow.getExceptionHandlerSet().add(h);
    }

    /**
     * Create default decision states.
     *
     * @param flow the flow
     */
    protected void createDefaultDecisionStates(final Flow flow) {
        createDecisionState(flow, "serviceUnauthorizedCheck", "flowScope.unauthorizedRedirectUrl != null",
                "viewRedirectToUnauthorizedUrlView", "viewServiceErrorView");

        createDecisionState(flow, "serviceCheck", "flowScope.service != null",
                "generateServiceTicket", "viewGenericLoginSuccess");

        createDecisionState(flow, "postRedirectDecision", "requestScope.response.responseType.name() == 'POST'",
                "postView", "redirectView");

        createDecisionState(flow, "warn", "flowScope.warnCookieValue", "showWarningView", "redirect");

        createDecisionState(flow, "gatewayRequestCheck", 
                "requestParameters.gateway != '' and requestParameters.gateway != null and flowScope.service != null", 
                "gatewayServicesManagementCheck", "serviceAuthorizationCheck");

        createDecisionState(flow, "hasServiceCheck",
                "flowScope.service != null",
                "renewRequestCheck", "viewGenericLoginSuccess");

        createDecisionState(flow, "renewRequestCheck",
                "requestParameters.renew != '' and requestParameters.renew != null",
                "serviceAuthorizationCheck", "generateServiceTicket");
    }
}

