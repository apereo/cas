package org.jasig.cas.support.oauth.ticket.accesstoken;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.TicketFactory;

/**
 * Factory to create OAuth access tokens.
 *
 * @author Jerome Leleu
 * @since 4.3.0
 */
public interface AccessTokenFactory extends TicketFactory {

    AccessToken create(Service service, Authentication authentication);
}
