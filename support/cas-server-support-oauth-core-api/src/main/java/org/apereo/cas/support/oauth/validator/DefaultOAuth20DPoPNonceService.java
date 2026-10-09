package org.apereo.cas.support.oauth.validator;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;

/**
 * Keeps DPoP nonces as transient session tickets of their own.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class DefaultOAuth20DPoPNonceService extends BaseOAuth20NonceService implements OAuth20DPoPNonceService {
    private static final String PROPERTY_DPOP_NONCE = "dpopNonce";

    public DefaultOAuth20DPoPNonceService(final TicketRegistry ticketRegistry, final TicketFactory ticketFactory,
                                          final CasConfigurationProperties casProperties) {
        super(ticketRegistry, ticketFactory, casProperties);
    }

    @Override
    public boolean isEnabled() {
        return casProperties.getAuthn().getOidc().getDpop().getNonce().isEnabled();
    }

    @Override
    protected String getMarkerProperty() {
        return PROPERTY_DPOP_NONCE;
    }

    @Override
    protected Duration getTimeToLive() {
        return Beans.newDuration(casProperties.getAuthn().getOidc().getDpop().getNonce().getTimeToLive());
    }
}
