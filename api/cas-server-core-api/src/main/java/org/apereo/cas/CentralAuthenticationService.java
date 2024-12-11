package org.apereo.cas;

import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.validation.Assertion;

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
     * Default bean name.
     */
    String BEAN_NAME = "centralAuthenticationService";

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
     * @throws Throwable the throwable
     */
    Ticket createTicketGrantingTicket(AuthenticationResult authenticationResult) throws Throwable;

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
     * @throws Throwable the throwable
     */
    Ticket grantServiceTicket(String ticketGrantingTicketId, Service service, AuthenticationResult authenticationResult) throws Throwable;

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
     */
    Ticket grantProxyTicket(String proxyGrantingTicket, Service service);

    /**
     * Validate a {@link ServiceTicket} for a particular Service.
     *
     * @param serviceTicketId Proof of prior authentication.
     * @param service         Service wishing to validate a prior authentication.
     * @return Non -null ticket validation assertion.
     * @throws Throwable the throwable
     */
    Assertion validateServiceTicket(String serviceTicketId, Service service) throws Throwable;

    /**
     * Delegate a {@link TicketGrantingTicket}  to a Service for proxying authentication
     * to other Services.
     *
     * @param serviceTicketId      The service ticket identifier that will delegate to a {@link TicketGrantingTicket}.
     * @param authenticationResult The current authentication context before this ticket can be granted.
     * @return Non -null ticket-granting ticket identifier that can grant {@link ServiceTicket} that proxy authentication.
     * @throws Throwable the throwable
     */
    Ticket createProxyGrantingTicket(String serviceTicketId, AuthenticationResult authenticationResult) throws Throwable;

    /**
     * Gets ticket factory.
     *
     * @return the ticket factory
     */
    TicketFactory getTicketFactory();
}
