package org.apereo.cas.ticket.code;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.TicketFactory;

/**
 * Factory to create OAuth codes.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public interface OAuthCodeFactory extends TicketFactory {

    /**
     * Create an OAuth code.
     *
     * @param service the service
     * @param authentication the authentication
     * @return the OAuth code
     */
    OAuthCode create(Service service, Authentication authentication);
}
