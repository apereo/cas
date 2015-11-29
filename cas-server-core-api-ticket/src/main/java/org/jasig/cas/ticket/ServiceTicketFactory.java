package org.jasig.cas.ticket;

import org.jasig.cas.authentication.principal.Service;

/**
 * The {@link TicketGrantingTicketFactory} is responsible for
 * creating instances of {@link TicketGrantingTicket}.
 *
 * @author Misagh Moayyed
 * @param <T> the type parameter
 * @since 4.2
 */
public interface ServiceTicketFactory<T extends Ticket> extends TicketFactory {

    /**
     * Create the ticket object.
     *
     * @param <T>                        the type parameter
     * @param service                    the service
     * @param expirationPolicy           the expiration policy
     * @param credentialsProvided        the credentials provided
     * @param onlyTrackMostRecentSession the only track most recent session
     * @return the t
     */
    <T extends Ticket> T create(Service service,
                                ExpirationPolicy expirationPolicy,
                                boolean credentialsProvided,
                                boolean onlyTrackMostRecentSession);
}
