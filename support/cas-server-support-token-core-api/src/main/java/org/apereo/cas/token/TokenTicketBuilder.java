package org.apereo.cas.token;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This is {@link TokenTicketBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public interface TokenTicketBuilder {
    /**
     * Implementation bean name.
     */
    String BEAN_NAME = "tokenTicketBuilder";

    /**
     * Build token for a service ticket.
     *
     * @param serviceTicketId the ticket id
     * @param service         the service
     * @return the token identifier
     * @throws Throwable the throwable
     */
    String build(String serviceTicketId, WebApplicationService service) throws Throwable;

    /**
     * Build token for a ticket-granting ticket.
     *
     * @param ticketGrantingTicket the ticket granting ticket
     * @param claims               the claims
     * @return the string
     * @throws Throwable the throwable
     */
    default String build(final AuthenticationAwareTicket ticketGrantingTicket, final Map<String, List<Object>> claims) throws Throwable {
        return build(ticketGrantingTicket.getAuthentication(), null, ticketGrantingTicket.getId(), claims);
    }

    /**
     * Build token based on authentication alone.
     * The resulting token will contain a random UUID as its id.
     *
     * @param authentication the authentication
     * @return the string
     * @throws Throwable the throwable
     */
    default String build(final Authentication authentication) throws Throwable {
        return build(authentication, null);
    }

    /**
     * Build token.
     *
     * @param authentication    the authentication
     * @param registeredService the registered service
     * @return the string
     * @throws Throwable the throwable
     */
    default String build(final Authentication authentication, final RegisteredService registeredService) throws Throwable {
        return build(authentication, registeredService, UUID.randomUUID().toString(), Map.of());
    }

    /**
     * Build token for authentication attempt.
     * Token claims are expected to include authentication and principal attributes.
     *
     * @param authentication    the authentication
     * @param registeredService the registered service
     * @param jwtIdentifier     the jwt identifier
     * @param claims            the claims
     * @return the token
     * @throws Throwable the throwable
     */
    String build(Authentication authentication,
                 RegisteredService registeredService,
                 String jwtIdentifier,
                 Map<String, List<Object>> claims) throws Throwable;
}
