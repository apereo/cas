package org.jasig.cas.remoting.server;

import java.util.List;

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

/**
 * Wrapper implementation around a CentralAuthenticationService that does 
 * web-service specific "stuff" before delegating to the CentralAuthenticationService.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class CentralAuthenticationServiceRemoteService extends
    ServletEndpointSupport implements CentralAuthenticationService {

    /** The CORE to delegate to. */
    private CentralAuthenticationService centralAuthenticationService = (CentralAuthenticationService) getApplicationContext().getBean("centralAuthenticationService");
    
    /** The validators to check the Credentials. */
    private Validator[] validators = (Validator[]) ((List) getApplicationContext().getBean("validators")).toArray();

    public String createTicketGrantingTicket(final Credentials credentials)
        throws TicketCreationException {
        
        if (credentials == null) {
            throw new IllegalArgumentException("credentials is a required field.");
        }
        
        final Errors errors = validateCredentials(credentials);
        if (errors.hasErrors()) {
            throw new TicketCreationException("Validation of Credentials Error: " + errors.toString());
        }
        
        return this.centralAuthenticationService.createTicketGrantingTicket(credentials);
    } 

    public String grantServiceTicket(final String ticketGrantingTicketId,
        final Service service) throws TicketCreationException {
        
        return this.centralAuthenticationService.grantServiceTicket(ticketGrantingTicketId, service);
    }

    public String grantServiceTicket(final String ticketGrantingTicketId,
        final Service service, final Credentials credentials)
        throws TicketCreationException {
        
        if (credentials != null) {
            final Errors errors = validateCredentials(credentials);
            if (errors.hasErrors()) {
                throw new TicketCreationException("Validation of Credentials Error: " + errors.toString());
            }
        }
        
        return this.centralAuthenticationService.grantServiceTicket(ticketGrantingTicketId, service, credentials);
    }

    public Assertion validateServiceTicket(final String serviceTicketId,
        final Service service) throws TicketException {
        return this.centralAuthenticationService.validateServiceTicket(serviceTicketId, service);
    }

    public void destroyTicketGrantingTicket(final String ticketGrantingTicketId) {
        this.centralAuthenticationService.destroyTicketGrantingTicket(ticketGrantingTicketId);
    }

    public String delegateTicketGrantingTicket(final String serviceTicketId,
        final Credentials credentials) throws TicketException {

        final Errors errors = validateCredentials(credentials);
        if (errors.hasErrors()) {
            throw new TicketCreationException("Validation of Credentials Error: " + errors.toString());
        }
        
        return this.centralAuthenticationService.delegateTicketGrantingTicket(serviceTicketId, credentials);
    }
    
    private Errors validateCredentials(Credentials credentials) {
        final Errors errors = new BindException(credentials, "credentials");
        for (int i = 0; i < this.validators.length; i++) {
            ValidationUtils.invokeValidator(this.validators[i], credentials, errors);
        }
        
        return errors;
    }
}
