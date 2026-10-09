package org.apereo.cas.oidc.authn;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.support.oauth.validator.BaseOAuth20NonceService;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import lombok.val;

/**
 * Client attestation challenges, kept as transient session tickets of their own.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public class OidcClientAttestationDefaultChallengeService extends BaseOAuth20NonceService
    implements OidcClientAttestationChallengeService {
    private static final String PROPERTY_CHALLENGE = "clientAttestationChallenge";

    public OidcClientAttestationDefaultChallengeService(final TicketRegistry ticketRegistry, final TicketFactory ticketFactory,
                                                        final CasConfigurationProperties casProperties) {
        super(ticketRegistry, ticketFactory, casProperties);
    }

    @Override
    public boolean isEnabled() {
        val clientAttestation = casProperties.getAuthn().getOidc().getClientAttestation();
        return clientAttestation.getChallenge().isEnabled() && !clientAttestation.getTrustAnchors().isEmpty();
    }

    @Override
    protected String getMarkerProperty() {
        return PROPERTY_CHALLENGE;
    }

    @Override
    protected Duration getTimeToLive() {
        return Beans.newDuration(casProperties.getAuthn().getOidc().getClientAttestation().getChallenge().getTimeToLive());
    }
}
