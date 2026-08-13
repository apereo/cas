package org.apereo.cas.oidc.vc.offer;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
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
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(configurationContext.getServicesManager(),
            clientId, OidcRegisteredService.class);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);
        val transientFactory = (TransientSessionTicketFactory) configurationContext.getTicketFactory().get(TransientSessionTicket.class);

        val codeProperties = new LinkedHashMap<>();
        codeProperties.put("principalId", principalId);
        codeProperties.put(OAuth20Constants.CLIENT_ID, clientId);
        codeProperties.put("credentialConfigurationIds", credentialConfigurationIds);
        
        val properties = new LinkedHashMap<>();
        properties.put("issuerState", UUID.randomUUID().toString());
        properties.put("principalId", principalId);
        properties.put(OAuth20Constants.CLIENT_ID, clientId);
        properties.put("credentialConfigurationIds", credentialConfigurationIds);
        
        return FunctionUtils.doUnchecked(() -> {
            val preAuthorizationCode = transientFactory.create(codeProperties);
            val transaction = transientFactory.create(properties);

            transaction.putProperty("preAuthorizedCode", preAuthorizationCode.getId());
            preAuthorizationCode.putProperty("transactionId", transaction.getId());

            configurationContext.getTicketRegistry().addTicket(preAuthorizationCode);
            return configurationContext.getTicketRegistry().addTicket(transaction);
        });
    }

    @Override
    public @Nullable Ticket fetch(final String transactionId) {
        val ticket = (TransientSessionTicket) configurationContext.getTicketRegistry().getTicket(transactionId);
        return ticket != null && !ticket.isExpired() ? ticket : null;
    }

    @Override
    public @Nullable Ticket fetchPreAuthorizationCode(final String preAuthorizationCode) {
        val ticket = (TransientSessionTicket) configurationContext.getTicketRegistry().getTicket(preAuthorizationCode);
        return ticket != null && !ticket.isExpired() ? ticket : null;
    }
}
