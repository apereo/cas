package org.apereo.cas.interrupt.webflow.actions;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.interrupt.webflow.InterruptUtils;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.net.URI;

/**
 * This is {@link FinalizeInterruptFlowAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class FinalizeInterruptFlowAction extends AbstractAction {
    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final RegisteredService registeredService = WebUtils.getRegisteredService(requestContext);
        final InterruptResponse response = InterruptUtils.getInterruptFrom(requestContext);

        if (response.isBlock()) {
            final URI accessUrl = registeredService != null
                ? registeredService.getAccessStrategy().getUnauthorizedRedirectUrl()
                : null;
            if (accessUrl != null) {
                final String url = accessUrl.toURL().toExternalForm();
                final ExternalContext externalContext = requestContext.getExternalContext();
                externalContext.requestExternalRedirect(url);
                externalContext.recordResponseComplete();
                return no();
            }
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, "Denied");
        }
        final Authentication authentication = WebUtils.getAuthentication(requestContext);
        authentication.addAttribute("finalizedInterrupt", Boolean.TRUE);
        return success();
    }
}
