package org.apereo.cas.oidc.vc.issuer.nonce;

import module java.base;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * This is {@link OidcVerifiableCredentialDefaultNonceService}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcVerifiableCredentialDefaultNonceService implements OidcVerifiableCredentialNonceService {
    private final OidcConfigurationContext configurationContext;

    @Override
    public VerifiableCredentialNonce create() {
        return FunctionUtils.doUnchecked(() -> {
            val transientFactory = (TransientSessionTicketFactory) configurationContext.getTicketFactory()
                .get(TransientSessionTicket.class);
            val ticket = transientFactory.create(Map.of());
            val seconds = Beans.newDuration(configurationContext.getCasProperties().getAuthn()
                .getOidc().getVc().getIssuer().getNonceTtl()).toSeconds();
            val expiresAt = Instant.now(Clock.systemUTC()).plusSeconds(seconds);
            ticket.setExpirationPolicy(new HardTimeoutExpirationPolicy(seconds));
            configurationContext.getTicketRegistry().addTicket(ticket);
            LOGGER.debug("Added nonce [{}] to expire in [{}] seconds at [{}]",
                ticket.getId(), seconds, expiresAt);
            return new VerifiableCredentialNonce(ticket.getId(), expiresAt);
        });
    }

    @Override
    public void remove(final String nonce) {
        FunctionUtils.doUnchecked(_ -> configurationContext.getTicketRegistry().deleteTicket(nonce));
    }

    @Override
    public boolean exists(final String nonce) {
        return FunctionUtils.doUnchecked(() -> {
            val ticket = configurationContext.getTicketRegistry().getTicket(nonce);
            LOGGER.debug("Found nonce ticket [{}} for [{}]", ticket, nonce);
            return ticket != null && !ticket.isExpired();
        });
    }
}
