package org.jasig.cas;

import com.google.common.base.Predicate;
import org.jasig.cas.authentication.AuthenticationContext;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.logout.LogoutRequest;
import org.jasig.cas.ticket.AbstractTicketException;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyTicket;
import org.jasig.cas.validation.Assertion;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;

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
     * Create a {@link org.jasig.cas.ticket.TicketGrantingTicket} by authenticating credentials.
     * The details of the security policy around credential authentication and the definition
     * of authentication success are dependent on the implementation, but it SHOULD be safe to assume
     * that at least one credential MUST be authenticated for ticket creation to succeed.
     *
     * @param context the current authentication context in order to create the ticket.
     * @return Non -null ticket-granting ticket identifier.
     * @throws AuthenticationException on errors authenticating the credentials
     * @throws AbstractTicketException if ticket cannot be created
     */
    TicketGrantingTicket createTicketGrantingTicket(@NotNull AuthenticationContext context)
        throws AuthenticationException, AbstractTicketException;


    /**
     * Obtains the given ticket by its id and type
     * and returns the CAS-representative object. Implementations
     * need to check for the validity of the ticket by making sure
     * it exists and has not expired yet, etc. This method is specifically
     * designed to remove the need to access the ticket registry.
     *
     * @param <T>      the generic ticket type to return that extends {@link Ticket}
     * @param ticketId the ticket granting ticket id
     * @param clazz    the ticket type that is reques to be found
     * @return the ticket object
     * @throws InvalidTicketException the invalid ticket exception
     * @since 4.1.0
     */
    <T extends Ticket> T getTicket(@NotNull String ticketId, @NotNull Class<? extends Ticket> clazz)
            throws InvalidTicketException;

    /**
     * Retrieve a collection of tickets from the underlying ticket registry.
     * The retrieval operation must pass the predicate check that is solely
     * used to filter the collection of tickets received. Implementations
     * can use the predicate to request a collection of expired tickets,
     * or tickets whose id matches a certain pattern, etc. The resulting
     * collection will include ticktes that have been evaluated by the predicate.
     *
     * @param predicate the predicate
     * @return the tickets
     * @since 4.1.0
     */
    Collection<Ticket> getTickets(@NotNull Predicate<Ticket> predicate);

    /**
     * Grant a {@link org.jasig.cas.ticket.ServiceTicket} that may be used to access the given service
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
     * @param context             The authentication context established if credentials provided
     * @return Non -null service ticket identifier.
     * @throws AuthenticationException on errors authenticating the credentials
     * @throws AbstractTicketException if the ticket could not be created.
     */
    ServiceTicket grantServiceTicket(
        @NotNull String ticketGrantingTicketId, @NotNull Service service, AuthenticationContext context)
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
     * @param service                The target service of the ServiceTicket.
     * @return Non -null service ticket identifier.
     * @throws AbstractTicketException if the ticket could not be created.
     */
    ProxyTicket grantProxyTicket(
            @NotNull String proxyGrantingTicket, @NotNull Service service)
            throws AbstractTicketException;

    /**
     * Validate a ServiceTicket for a particular Service.
     *
     * @param serviceTicketId Proof of prior authentication.
     * @param service         Service wishing to validate a prior authentication.
     * @return Non -null ticket validation assertion.
     * @throws AbstractTicketException if there was an error validating the ticket.
     */
    Assertion validateServiceTicket(@NotNull String serviceTicketId, @NotNull Service service) throws AbstractTicketException;

    /**
     * Destroy a TicketGrantingTicket and perform back channel logout. This has the effect of invalidating any
     * Ticket that was derived from the TicketGrantingTicket being destroyed. May throw an
     * {@link IllegalArgumentException} if the TicketGrantingTicket ID is null.
     *
     * @param ticketGrantingTicketId the id of the ticket we want to destroy
     * @return the logout requests.
     */
    List<LogoutRequest> destroyTicketGrantingTicket(@NotNull String ticketGrantingTicketId);

    /**
     * Delegate a TicketGrantingTicket to a Service for proxying authentication
     * to other Services.
     *
     * @param serviceTicketId The service ticket identifier that will delegate to a {@link org.jasig.cas.ticket.TicketGrantingTicket}.
     * @param context     The current authentication context before this ticket can be granted.
     * @return Non -null ticket-granting ticket identifier that can grant
     * {@link org.jasig.cas.ticket.ServiceTicket} that proxy authentication.
     * @throws AuthenticationException on errors authenticating the credentials
     * @throws AbstractTicketException if there was an error creating the ticket
     */
    ProxyGrantingTicket createProxyGrantingTicket(@NotNull String serviceTicketId, @NotNull AuthenticationContext context)
            throws AuthenticationException, AbstractTicketException;
}
