package org.jasig.cas.remoting.server;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.TicketCreationException;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.validation.Assertion;
import org.springframework.remoting.jaxrpc.ServletEndpointSupport;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;


public class CentralAuthenticationServiceRemoteService extends
    ServletEndpointSupport implements CentralAuthenticationService {
    private CentralAuthenticationService centralAuthenticationService;
    private Validator[] validators;

    public String createTicketGrantingTicket(final Credentials credentials)
        throws TicketCreationException {
        
        if (credentials == null) {
            throw new IllegalArgumentException("credentials is a required field.");
        }
        
        final Errors errors = new BindException(credentials, "credentials");
        for (int i = 0; i < this.validators.length; i++) {
            ValidationUtils.invokeValidator(this.validators[i], credentials, errors);
        }
        
        if (errors.hasErrors()) {
            throw new TicketCreationException("Validation of Credentials Error: " + errors.toString());
        }
        
        return this.centralAuthenticationService.createTicketGrantingTicket(credentials);
    }

        

    public String grantServiceTicket(final String ticketGrantingTicketId,
        final Service service) throws TicketCreationException {
        if (ticketGrantingTicketId == null || service == null) {
            throw new IllegalArgumentException("ticketGrantingTicketId and service are required fields.");
        }
        
        return this.centralAuthenticationService.grantServiceTicket(ticketGrantingTicketId, service);
    }

    public String grantServiceTicket(final String ticketGrantingTicketId,
        final Service service, final Credentials credentials)
        throws TicketCreationException {
        if (ticketGrantingTicketId == null || service == null || credentials == null) {
            throw new IllegalArgumentException("ticketGrantingTicketId, credentials and service are required fields.");
        }
        
        final Errors errors = new BindException(credentials, "credentials");
        for (int i = 0; i < this.validators.length; i++) {
            ValidationUtils.invokeValidator(this.validators[i], credentials, errors);
        }
        
        if (errors.hasErrors()) {
            throw new TicketCreationException("Validation of Credentials Error: " + errors.toString());
        }
        
        return this.centralAuthenticationService.grantServiceTicket(ticketGrantingTicketId, service, credentials);
    }

    public Assertion validateServiceTicket(String serviceTicketId,
        Service service) throws TicketException {
        // TODO Auto-generated method stub
        return null;
    }

    public void destroyTicketGrantingTicket(final String ticketGrantingTicketId) {
        if (ticketGrantingTicketId == null) {
            throw new IllegalArgumentException("ticketGrantingTicketId cannot be null");
        }
        
        this.centralAuthenticationService.destroyTicketGrantingTicket(ticketGrantingTicketId);
    }

    public String delegateTicketGrantingTicket(String serviceTicketId,
        Credentials credentials) throws TicketException {
        // TODO Auto-generated method stub
        return null;
    }

}
