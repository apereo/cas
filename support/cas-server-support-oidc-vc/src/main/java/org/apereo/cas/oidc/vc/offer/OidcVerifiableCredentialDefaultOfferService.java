package org.apereo.cas.oidc.vc.offer;

import module java.base;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.util.function.FunctionUtils;
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

    /**
     * Create the offer from the stored transaction. A registry that encodes tickets, the stateless registry,
     * stores an encoded ticket rather than the transaction itself, so the transaction is read back by its id.
     *
     * @param clientId                   the client id
     * @param principalId                the principal id
     * @param credentialConfigurationIds the credential configuration ids
     * @return the credential offer
     */
    @Override
    public OidcVerifiableCredentialOffer create(final String clientId, final String principalId, final List<String> credentialConfigurationIds) {
        val storedTransaction = Objects.requireNonNull(transactionService.issue(clientId, principalId, credentialConfigurationIds));
        val transaction = storedTransaction instanceof final TransientSessionTicket transientTicket
            ? transientTicket
            : (TransientSessionTicket) transactionService.fetch(storedTransaction.getId());
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
        val credentialConfigurationIds = OidcVerifiableCredentialTransactionService.getCredentialConfigurationIds(transaction);
        val issuer = configurationContext.getCasProperties().getAuthn().getOidc().getCore().getIssuer();

        val transactionCode = Objects.requireNonNull(transaction)
            .getPropertyAsString(OidcVerifiableCredentialTransactionService.PROPERTY_TRANSACTION_CODE);

        val grant = new OidcVerifiableCredentialOffer.Grants.PreAuthorizedCodeGrant();
        FunctionUtils.doIfNotBlank(transactionCode, _ -> grant.setTransactionCode(
            OidcVerifiableCredentialOffer.Grants.TransactionCode
                .builder()
                .value(transactionCode)
                .length(transactionCode.length())
                .build()
        ));
        grant.setPreAuthorizedCode(transaction.getPropertyAsString("preAuthorizedCode"));
        grant.setIssuerState(transaction.getPropertyAsString("issuerState"));

        val grants = new OidcVerifiableCredentialOffer.Grants();
        grants.setPreAuthorizedCodeGrant(grant);

        val offer = new OidcVerifiableCredentialOffer();
        offer.setTransactionId(transaction.getId());
        offer.setCredentialIssuer(issuer);
        offer.setCredentialConfigurationIds(credentialConfigurationIds);
        offer.setGrants(grants);
        return offer;
    }
}
