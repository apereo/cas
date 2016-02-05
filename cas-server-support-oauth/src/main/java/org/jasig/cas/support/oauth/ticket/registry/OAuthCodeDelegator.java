package org.jasig.cas.support.oauth.ticket.registry;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.support.oauth.ticket.code.OAuthCode;
import org.jasig.cas.ticket.registry.AbstractTicketRegistry;
import org.jasig.cas.ticket.registry.ServiceTicketDelegator;

/**
 * This is a specific delegator for {@link OAuthCode}.
 *
 * @author Jerome Leleu
 * @since 4.3.0
 */
public class OAuthCodeDelegator<T extends OAuthCode> extends ServiceTicketDelegator<T>
        implements OAuthCode {

    private static final long serialVersionUID = 793510783336656110L;

    /**
     * Instantiates a new OAuth code delegator.
     *
     * @param ticketRegistry the ticket registry
     * @param oauthCode the OAuth code
     * @param callback the callback
     */
    public OAuthCodeDelegator(final AbstractTicketRegistry ticketRegistry,
                       final T oauthCode, final boolean callback) {
        super(ticketRegistry, oauthCode, callback);
    }

    @Override
    public Authentication getAuthentication() {
        return getTicket().getAuthentication();
    }
}
