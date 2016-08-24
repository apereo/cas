package org.apereo.cas.web.flow;

import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.services.UnauthorizedSsoServiceException;
import org.springframework.webflow.engine.Flow;
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
        
        final TransitionExecutingFlowExecutionExceptionHandler h = new TransitionExecutingFlowExecutionExceptionHandler();
        h.add(UnauthorizedSsoServiceException.class, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
        h.add(NoSuchFlowExecutionException.class, "viewServiceErrorView");
        h.add(UnauthorizedServiceException.class, "serviceUnauthorizedCheck");
        flow.getExceptionHandlerSet().add(h);
    }
}

