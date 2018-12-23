package org.apereo.cas.uma.web.authn;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link UmaRequestingPartyTokenAuthenticator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class UmaRequestingPartyTokenAuthenticator extends BaseUmaTokenAuthenticator {

    public UmaRequestingPartyTokenAuthenticator(final TicketRegistry ticketRegistry) {
        super(ticketRegistry);
    }

    @Override
    protected String getRequiredScope() {
        return OAuth20Constants.UMA_PROTECTION_SCOPE;
    }
}
