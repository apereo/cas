package org.apereo.cas;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.logout.LogoutRequest;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.validation.Assertion;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * CAS viewed as a set of services to generate and validate Tickets.
 * <p>
 * This is the interface between a Web HTML, Web Services, RMI, or any other
 * request processing layer and the CAS Service viewed as a mechanism to
 * generate, store, validate, and retrieve Tickets containing Authentication
 * information. The features of the request processing layer (the HttpXXX
 * Servlet objects) are not visible here or in any modules behind this layer. In
 * theory, a standalone application could call these methods directly as a
 * private authentication service.
 * </p>
 *
 * @author William G. Thompson, Jr.
 * @author Dmitry Kopylenko
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 3.0.0
 */
public interface CentralAuthenticationService {

    /**
     * CAS namespace.
     */
    String NAMESPACE = CentralAuthenticationService.class.getPackage().getName();
    
    /**
     * Create a {@link TicketGrantingTicket} by authenticating credentials.
     * The details of the security policy around credential authentication and the definition
     * of authentication success are dependent on the implementation, but it SHOULD be safe to assume
     * that at least one credential MUST be authenticated for ticket creation to succeed.
     *
     * @param authenticationResult the current authentication result in order to create the ticket.
     * @return Non -null ticket-granting ticket identifier.
     * @throws AuthenticationException on errors authenticating the credentials
     * @throws AbstractTicketException if ticket cannot be created
     */
    TicketGrantingTicket createTicketGrantingTicket(AuthenticationResult authenticationResult)
        throws AuthenticationException, AbstractTicketException;


    /**
     * Updates the ticket instance in the underlying storage mechanism.
     * The properties of a given ticket, such as its authentication attributes
     * may have changed during various legs of the authentication flow.
     *
     * @param ticket the ticket
     * @return the updated ticket
     * @since 5.0.0
     */
    Ticket updateTicket(Ticket ticket);

    /**
     * Obtains the given ticket by its id
     * and returns the CAS-representative object. Implementations
     * need to check for the validity of the ticket by making sure
     * it exists and has not expired yet, etc. This method is specifically
     * designed to remove the need to access the ticket registry.
     *
     * @param <T>      the generic ticket type to return that extends {@link Ticket}
     * @param ticketId the ticket granting ticket id
     * @return the ticket object
     * @throws InvalidTicketException the invalid ticket exception
     * @since 5.0.0
     */
    <T extends Ticket> T getTicket(String ticketId) throws InvalidTicketException;

    /**
     * Obtains the given ticket by its id and type
     * and returns the CAS-representative object. Implementations
     * need to check for the validity of the ticket by making sure
     * it exists and has not expired yet, etc. This method is specifically
     * designed to remove the need to access the ticket registry.
     *
     * @param <T>      the generic ticket type to return that extends {@link Ticket}
     * @param ticketId the ticket granting ticket id
     * @param clazz    the ticket type that is requested to be found
     * @return the ticket object
     * @throws InvalidTicketException the invalid ticket exception
     * @since 4.1.0
     */
    <T extends Ticket> T getTicket(String ticketId, Class<T> clazz)
            throws InvalidTicketException;

    /**
     * Attempts to delete a ticket from the underlying store
     * and is allowed to run any number of processing on the ticket
     * and removal op before invoking it. The ticket id can be associated
     * with any ticket type that is valid and understood by CAS and the underlying
     * ticket store; however some special cases require that you invoke the appropriate
     * operation when destroying tickets, such {@link #destroyTicketGrantingTicket(String)}.
     *
     * @param ticketId the ticket id
     */
    default void deleteTicket(String ticketId) {}

    /**
     * Retrieve a collection of tickets from the underlying ticket registry.
     * The retrieval operation must pass the predicate check that is solely
     * used to filter the collection of tickets received. Implementations
     * can use the predicate to request a collection of expired tickets,
     * or tickets whose id matches a certain pattern, etc. The resulting
     * collection will include tickets that have been evaluated by the predicate.
     *
     * @param predicate the predicate
     * @return the tickets
     * @since 4.1.0
     */
    Collection<Ticket> getTickets(Predicate<Ticket> predicate);

    /**
     * Grant a {@link ServiceTicket} that may be used to access the given service
     * by authenticating the given credentials.
     * The details of the security policy around credential authentication and the definition
     * of authentication success are dependent on the implementation, but it SHOULD be safe to assume
     * that at least one credential MUST be authenticated for ticket creation to succeed.
     * <p>
     * The principal that is resolved from the authenticated credentials MUST be the same as that to which
     * the given ticket-granting ticket was issued.
     * </p>
     *
     * @param ticketGrantingTicketId Proof of prior authentication.
     * @param service                The target service of the ServiceTicket.
     * @param authenticationResult   The authentication context established if credentials provided
     * @return Non -null service ticket identifier.
     * @throws AuthenticationException on errors authenticating the credentials
     * @throws AbstractTicketException if the ticket could not be created.
     */
    ServiceTicket grantServiceTicket(
            String ticketGrantingTicketId, Service service, AuthenticationResult authenticationResult)
            throws AuthenticationException, AbstractTicketException;

    /**
     * Grant a {@link ProxyTicket} that may be used to access the given service
     * by authenticating the given credentials.
     * The details of the security policy around credential authentication and the definition
     * of authentication success are dependent on the implementation, but it SHOULD be safe to assume
     * that at least one credential MUST be authenticated for ticket creation to succeed.
     * <p>
     * The principal that is resolved from the authenticated credentials MUST be the same as that to which
     * the given ticket-granting ticket was issued.
     * </p>
     *
     * @param proxyGrantingTicket Proof of prior authentication.
     * @param service             The target service of the ServiceTicket.
     * @return Non -null service ticket identifier.
     * @throws AbstractTicketException if the ticket could not be created.
     */
    ProxyTicket grantProxyTicket(
             String proxyGrantingTicket, Service service)
            throws AbstractTicketException;

    /**
     * Validate a ServiceTicket for a particular Service.
     *
     * @param serviceTicketId Proof of prior authentication.
     * @param service         Service wishing to validate a prior authentication.
     * @return Non -null ticket validation assertion.
     * @throws AbstractTicketException if there was an error validating the ticket.
     */
    Assertion validateServiceTicket(String serviceTicketId, Service service) throws AbstractTicketException;

    /**
     * Destroy a TicketGrantingTicket and perform back channel logout. This has the effect of invalidating any
     * Ticket that was derived from the TicketGrantingTicket being destroyed. May throw an
     * {@link IllegalArgumentException} if the TicketGrantingTicket ID is null.
     *
     * @param ticketGrantingTicketId the id of the ticket we want to destroy
     * @return the logout requests.
     */
    List<LogoutRequest> destroyTicketGrantingTicket(String ticketGrantingTicketId);

    /**
     * Delegate a TicketGrantingTicket to a Service for proxying authentication
     * to other Services.
     *
     * @param serviceTicketId      The service ticket identifier that will delegate to a {@link TicketGrantingTicket}.
     * @param authenticationResult The current authentication context before this ticket can be granted.
     * @return Non -null ticket-granting ticket identifier that can grant {@link ServiceTicket} that proxy authentication.
     * @throws AuthenticationException on errors authenticating the credentials
     * @throws AbstractTicketException if there was an error creating the ticket
     */
    ProxyGrantingTicket createProxyGrantingTicket(String serviceTicketId, AuthenticationResult authenticationResult)
            throws AuthenticationException, AbstractTicketException;
}
