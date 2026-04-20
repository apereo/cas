package org.apereo.cas.oidc.vc.offer;

import module java.base;
import org.apereo.cas.ticket.Ticket;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link OidcVerifiableCredentialTransactionService}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public interface OidcVerifiableCredentialTransactionService {
    /**
     * Default bean name.
     */
    String BEAN_NAME = "oidcVerifiableCredentialTransactionService";

    /**
     * Issue ticket.
     *
     * @param clientId                   the client id
     * @param principalId                the principal id
     * @param credentialConfigurationIds the credential configuration ids
     * @return the transient session ticket
     */
    @Nullable Ticket issue(String clientId, String principalId, List<String> credentialConfigurationIds);

    /**
     * Consume ticket.
     *
     * @param transactionId the transaction id
     * @return the transient session ticket
     */
    @Nullable Ticket fetch(String transactionId);
}
