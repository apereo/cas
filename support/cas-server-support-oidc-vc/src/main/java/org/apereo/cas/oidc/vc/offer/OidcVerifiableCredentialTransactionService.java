package org.apereo.cas.oidc.vc.offer;

import module java.base;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TransientSessionTicket;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
     * Ticket property that carries the transaction code. The code is a secret
     * that is delivered to the End-User out of band and is never disclosed in
     * the credential offer document.
     */
    String PROPERTY_TRANSACTION_CODE = "transactionCode";

    /**
     * Ticket property that carries the credential configuration ids the offer covers.
     */
    String PROPERTY_CREDENTIAL_CONFIGURATION_IDS = "credentialConfigurationIds";

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

    /**
     * Fetch pre authorization code ticket.
     *
     * @param preAuthorizationCode the pre authorization code
     * @return the ticket
     */
    @Nullable Ticket fetchPreAuthorizationCode(String preAuthorizationCode);

    /**
     * Redeem a pre-authorization code, atomically, so that only one of several concurrent redemptions
     * of the same code can succeed. OpenID4VCI 1.0 requires the code to be single use.
     *
     * @param preAuthorizationCode the pre authorization code
     * @return the consumed ticket, or null when the code is unknown, expired or already redeemed
     */
    @Nullable Ticket consumePreAuthorizationCode(String preAuthorizationCode);

    /**
     * Validate the transaction code presented against the one bound to the pre-authorization code.
     * A transaction code is mandatory whenever the issuance transaction defined one.
     *
     * @param preAuthorizationCode    the pre authorization code
     * @param providedTransactionCode the transaction code presented by the client
     * @return true/false
     */
    default boolean isTransactionCodeValid(final TransientSessionTicket preAuthorizationCode,
                                           @Nullable final String providedTransactionCode) {
        val expected = preAuthorizationCode.getPropertyAsString(PROPERTY_TRANSACTION_CODE);
        if (StringUtils.isBlank(expected)) {
            return StringUtils.isBlank(providedTransactionCode);
        }
        return StringUtils.isNotBlank(providedTransactionCode)
            && MessageDigest.isEqual(
            expected.getBytes(StandardCharsets.UTF_8),
            providedTransactionCode.getBytes(StandardCharsets.UTF_8));
    }
}
