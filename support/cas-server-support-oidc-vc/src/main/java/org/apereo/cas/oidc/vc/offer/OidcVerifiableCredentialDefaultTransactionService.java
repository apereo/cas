package org.apereo.cas.oidc.vc.offer;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link OidcVerifiableCredentialDefaultTransactionService}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcVerifiableCredentialDefaultTransactionService implements OidcVerifiableCredentialTransactionService {
    private final OidcConfigurationContext configurationContext;

    @Override
    public Ticket issue(final String clientId, final String principalId, final List<String> credentialConfigurationIds) {
        return FunctionUtils.doUnchecked(() -> {
            val transientFactory = (TransientSessionTicketFactory) configurationContext.getTicketFactory().get(TransientSessionTicket.class);
            val properties = new LinkedHashMap<>();
            properties.put("issuerState", UUID.randomUUID().toString());
            properties.put("principalId", principalId);
            properties.put(OAuth20Constants.CLIENT_ID, clientId);
            properties.put("credentialConfigurationIds", credentialConfigurationIds);
            properties.put("preAuthorizedCode", RandomUtils.randomNumeric(10));
            val ticket = transientFactory.create(properties);
            return configurationContext.getTicketRegistry().addTicket(ticket);
        });
    }

    @Override
    public @Nullable Ticket fetch(final String transactionId) {
        val ticket = (TransientSessionTicket) configurationContext.getTicketRegistry().getTicket(transactionId);
        return ticket != null && !ticket.isExpired() ? ticket : null;
    }
}
