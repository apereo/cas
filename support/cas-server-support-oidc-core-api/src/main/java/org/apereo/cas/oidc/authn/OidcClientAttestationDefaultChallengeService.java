package org.apereo.cas.oidc.authn;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * Keeps challenges as transient session tickets marked as challenges, expiring after the configured time to live, so they
 * are shared by all nodes that share the ticket registry. With the stateless ticket registry the challenge carries its own
 * expiration.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiredArgsConstructor
public class OidcClientAttestationDefaultChallengeService implements OidcClientAttestationChallengeService {
    private static final String PROPERTY_CHALLENGE = "clientAttestationChallenge";

    protected final TicketRegistry ticketRegistry;

    protected final TicketFactory ticketFactory;

    protected final CasConfigurationProperties casProperties;

    @Override
    public boolean isEnabled() {
        val clientAttestation = casProperties.getAuthn().getOidc().getClientAttestation();
        return clientAttestation.getChallenge().isEnabled() && !clientAttestation.getTrustAnchors().isEmpty();
    }

    @Override
    public String create() throws Throwable {
        val timeToLive = Beans.newDuration(casProperties.getAuthn().getOidc().getClientAttestation().getChallenge().getTimeToLive());
        val properties = new HashMap<String, Serializable>();
        properties.put(PROPERTY_CHALLENGE, Boolean.TRUE.toString());
        properties.put(ExpirationPolicy.class.getName(), new HardTimeoutExpirationPolicy(Math.max(1, timeToLive.toSeconds())));
        val factory = (TransientSessionTicketFactory) ticketFactory.get(TransientSessionTicket.class);
        return Objects.requireNonNull(ticketRegistry.addTicket(factory.create(properties))).getId();
    }

    @Override
    public boolean isValid(final @Nullable String challenge) {
        if (StringUtils.isBlank(challenge)) {
            return false;
        }
        val ticket = FunctionUtils.doAndHandle(() -> ticketRegistry.getTicket(challenge));
        return ticket instanceof final TransientSessionTicket transientTicket && !transientTicket.isExpired()
            && transientTicket.containsProperty(PROPERTY_CHALLENGE);
    }
}
