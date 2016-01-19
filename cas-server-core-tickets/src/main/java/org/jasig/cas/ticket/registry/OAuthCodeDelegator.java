package org.jasig.cas.ticket.registry;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.ticket.OAuthCode;

/**
 * This is a specific delegator for {@link OAuthCode}.
 *
 * @author Jerome Leleu
 * @since 4.3.0
 */
public class OAuthCodeDelegator<T extends OAuthCode> extends ServiceTicketDelegator<T>
        implements OAuthCode {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new OAuth code delegator.
     *
     * @param ticketRegistry the ticket registry
     * @param oauthCode the OAuth code
     * @param callback the callback
     */
    OAuthCodeDelegator(final AbstractTicketRegistry ticketRegistry,
                       final T oauthCode, final boolean callback) {
        super(ticketRegistry, oauthCode, callback);
    }

    @Override
    public Authentication getAuthentication() {
        return getTicket().getAuthentication();
    }
}
