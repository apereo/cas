package org.apereo.cas.oidc.vc.offer;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.ticket.TransientSessionTicket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;

/**
 * This is {@link OidcVerifiableCredentialDefaultOfferService}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcVerifiableCredentialDefaultOfferService implements OidcVerifiableCredentialOfferService {
    private final OidcConfigurationContext configurationContext;
    private final OidcVerifiableCredentialTransactionService transactionService;

    @Override
    public OidcVerifiableCredentialOffer create(final String principalId, final List<String> credentialConfigurationIds) {
        val transaction = (TransientSessionTicket) transactionService.issue(principalId, credentialConfigurationIds);
        return buildCredentialOffer(Objects.requireNonNull(transaction));
    }


    @Override
    public OidcVerifiableCredentialOffer fetch(final String transactionId) {
        val transaction = (TransientSessionTicket) transactionService.fetch(transactionId);
        if (transaction == null) {
            throw new IllegalArgumentException(String.format("No transaction found for [%s]", transactionId));
        }
        return buildCredentialOffer(transaction);
    }

    private @NonNull OidcVerifiableCredentialOffer buildCredentialOffer(final TransientSessionTicket transaction) {
        val credentialConfigurationIds = transaction.getProperty("credentialConfigurationIds", List.class);
        val issuer = configurationContext.getCasProperties().getAuthn().getOidc().getCore().getIssuer();
        val grant = new OidcVerifiableCredentialOffer.Grants.PreAuthorizedCodeGrant();
        grant.setTxCode(Objects.requireNonNull(transaction).getId());
        grant.setPreAuthorizedCode(transaction.getPropertyAsString("preAuthorizedCode"));
        grant.setIssuerState(transaction.getPropertyAsString("issuerState"));

        val grants = new OidcVerifiableCredentialOffer.Grants();
        grants.setPreAuthorizedCodeGrant(grant);

        val offer = new OidcVerifiableCredentialOffer();
        offer.setCredentialIssuer(issuer);
        offer.setCredentialConfigurationIds(credentialConfigurationIds);
        offer.setGrants(grants);
        return offer;
    }
}
