package org.jasig.cas.web.flow;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.client.util.URIBuilder;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.stereotype.Component;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.net.URI;

/**
 * This is {@link ServiceWarningAction}. Populates the view
 * with the target url of the application after the warning
 * screen is displayed. 
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Component("serviceWarningAction")
public class ServiceWarningAction extends AbstractAction {
    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final Service svc = WebUtils.getService(requestContext);
        final URIBuilder builder = new URIBuilder(svc.getId());
        final URI uri = builder.setParameter("service", svc.getId())
               .setParameter("ticket", WebUtils.getServiceTicketFromRequestScope(requestContext))
                .build();
        requestContext.getFlowScope().put("serviceWarningUrl", uri.toURL().toExternalForm());
        return null;
    }
}
