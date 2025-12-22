package org.apereo.cas.web.flow.login;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
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
public class InitializeLoginAction extends BaseCasWebflowAction {
    /**
     * The services manager with access to the registry.
     **/
    protected final ServicesManager servicesManager;

    /**
     * CAS Properties.
     */
    protected final CasConfigurationProperties casProperties;
    
    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        LOGGER.trace("Initialized login sequence");
        val service = WebUtils.getService(requestContext);
        if (service == null && !casProperties.getSso().getServices().isAllowMissingServiceParameter()) {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            LOGGER.warn("No service authentication request is available at [{}]. CAS is configured to disable the flow.", request.getRequestURL());
            throw new NoSuchFlowExecutionException(requestContext.getFlowExecutionContext().getKey(), UnauthorizedServiceException.required());
        }
        return success();
    }

}
