package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.authentication.principal.ResponseBuilderLocator;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link RedirectToServiceAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RedirectToServiceAction extends AbstractAction {

    private final ResponseBuilderLocator responseBuilderLocator;

    public RedirectToServiceAction(final ResponseBuilderLocator responseBuilderLocator) {
        this.responseBuilderLocator = responseBuilderLocator;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final WebApplicationService service = WebUtils.getService(requestContext);
        final String serviceTicketId = WebUtils.getServiceTicketFromRequestScope(requestContext);
        final Response response = responseBuilderLocator.locate(service).build(service, serviceTicketId);
        WebUtils.putServiceResponseIntoRequestScope(requestContext, response);
        WebUtils.putServiceOriginalUrlIntoRequestScope(requestContext, service);
        return new EventFactorySupport().event(this, response.getResponseType().name().toLowerCase());
    }
}
