package org.jasig.cas.support.oauth.ticket.refreshtoken;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.TicketFactory;

/**
 * Factory to create OAuth refresh tokens.
 *
 * @author Jerome Leleu
 * @since 4.3.0
 */
public interface RefreshTokenFactory extends TicketFactory {

    /**
     * Create a refresh token.
     *
     * @param service the service
     * @param authentication the authentication
     * @return the refresh token
     */
    RefreshToken create(Service service, Authentication authentication);
}
