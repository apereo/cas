package org.apereo.cas.oidc.vc.issuer.nonce;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.util.function.FunctionUtils;
import io.micrometer.common.util.StringUtils;
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
            configurationContext.getTicketRegistry().addTicket(ticket);
            val expiresIn = ticket.getExpirationPolicy().getTimeToLive();
            return new VerifiableCredentialNonce(ticket.getId(), expiresIn);
        });
    }

    @Override
    public int remove(final String nonce) {
        return StringUtils.isNotBlank(nonce)
            ? Objects.requireNonNull(FunctionUtils.doAndHandle(() -> configurationContext.getTicketRegistry().deleteTicket(nonce)))
            : 0;
    }

    @Override
    public boolean consume(final String nonce) {
        return FunctionUtils.doUnchecked(() -> {
            if (StringUtils.isBlank(nonce)) {
                return false;
            }
            val ticket = configurationContext.getTicketRegistry().getTicket(nonce);
            if (ticket == null || ticket.isExpired()) {
                LOGGER.debug("Nonce [{}] is unknown or has expired", nonce);
                return false;
            }
            return remove(nonce) > 0;
        });
    }

    @Override
    public boolean exists(final String nonce) {
        return FunctionUtils.doUnchecked(() -> {
            val ticket = configurationContext.getTicketRegistry().getTicket(nonce);
            LOGGER.debug("Found nonce ticket [{}] for [{}]", ticket, nonce);
            return ticket != null && !ticket.isExpired();
        });
    }
}
