package org.apereo.cas.web.flow.login;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;

/**
 * This is {@link InitializeLoginAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class InitializeLoginAction extends AbstractAction {
    /**
     * The services manager with access to the registry.
     **/
    protected final ServicesManager servicesManager;

    /**
     * CAS Properties.
     */
    protected final CasConfigurationProperties casProperties;
    
    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        LOGGER.trace("Initialized login sequence");
        val service = WebUtils.getService(requestContext);
        if (service == null && !casProperties.getSso().isAllowMissingServiceParameter()) {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            LOGGER.warn("No service authentication request is available at [{}]. CAS is configured to disable the flow.", request.getRequestURL());
            throw new NoSuchFlowExecutionException(requestContext.getFlowExecutionContext().getKey(),
                new UnauthorizedServiceException("screen.service.required.message", "Service is required"));
        }
        return success();
    }

    protected static boolean isLoginFlowActive(final RequestContext requestContext) {
        return requestContext.getActiveFlow().getId().equalsIgnoreCase(CasWebflowConfigurer.FLOW_ID_LOGIN);
    }
}
