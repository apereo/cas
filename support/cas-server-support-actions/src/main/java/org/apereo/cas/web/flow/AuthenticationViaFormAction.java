package org.apereo.cas.web.flow;

import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Action to authenticate credential and retrieve a TicketGrantingTicket for
 * those credential. If there is a request for renew, then it also generates
 * the Service Ticket required.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class AuthenticationViaFormAction extends AbstractAuthenticationAction {

    private CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;
    
    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        final Event serviceTicketEvent = this.serviceTicketRequestWebflowEventResolver.resolveSingle(requestContext);
        if (serviceTicketEvent != null) {
            return serviceTicketEvent;
        }
        return super.doExecuteInternal(requestContext);
    }

    public void setServiceTicketRequestWebflowEventResolver(final CasWebflowEventResolver r) {
        this.serviceTicketRequestWebflowEventResolver = r;
    }
}
