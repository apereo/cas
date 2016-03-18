package org.jasig.cas.support.oauth.ticket.code;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.TicketFactory;

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
