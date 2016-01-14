package org.jasig.cas.web.flow;

import org.jasig.cas.web.flow.authentication.CasWebflowEventResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.webflow.action.AbstractAction;
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
@Component("authenticationViaFormAction")
public class AuthenticationViaFormAction extends AbstractAction {

    @Autowired
    @Qualifier("loginTicketRequestValidationWebflowEventResolver")
    private CasWebflowEventResolver loginTicketRequestValidationWebflowEventResolver;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;


    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final Event event = this.loginTicketRequestValidationWebflowEventResolver.resolve(requestContext);
        if (event != null) {
            return event;
        }

        final Event serviceTicketEvent = this.serviceTicketRequestWebflowEventResolver.resolve(requestContext);
        if (serviceTicketEvent != null) {
            return serviceTicketEvent;
        }

        return initialAuthenticationAttemptWebflowEventResolver.resolve(requestContext);
    }
}
