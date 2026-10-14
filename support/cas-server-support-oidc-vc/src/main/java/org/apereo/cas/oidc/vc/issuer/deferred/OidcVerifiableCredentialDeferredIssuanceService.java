package org.apereo.cas.oidc.vc.issuer.deferred;

import module java.base;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialProofValidator.VerifiableCredentialProofResult;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jspecify.annotations.Nullable;

/**
 * Keeps the transactions of deferred credential issuance, per OpenID4VCI 1.0 section 9. A credential request for a
 * configuration with deferred issuance is validated as usual, and the validated holder keys are kept in a transaction
 * that is pending until it is approved or denied. The wallet collects the credentials from the deferred credential
 * endpoint once the transaction is approved; the credentials are built then, from the user's attributes at that time.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public interface OidcVerifiableCredentialDeferredIssuanceService {
    /**
     * Default bean name.
     */
    String BEAN_NAME = "oidcVerifiableCredentialDeferredIssuanceService";

    /**
     * Whether credentials of this configuration are issued in a deferred manner.
     *
     * @param credentialConfigurationId the credential configuration id
     * @return true when deferred
     */
    boolean isDeferred(String credentialConfigurationId);

    /**
     * Whether any published credential configuration is issued in a deferred manner, which is when the issuer advertises
     * the deferred credential endpoint.
     *
     * @return true when any configuration is deferred
     */
    boolean isDeferredIssuanceSupported();

    /**
     * Start a pending transaction for a credential request, bound to the client and the user of the access token.
     *
     * @param accessToken               the access token of the credential request
     * @param credentialConfigurationId the credential configuration id
     * @param proofs                    the validated proofs, one per holder key
     * @return the transaction, or empty when the ticket registry cannot keep it and the credentials are to be issued now
     * @throws Throwable the throwable
     */
    Optional<DeferredTransaction> defer(OAuth20AccessToken accessToken, String credentialConfigurationId,
                                        List<VerifiableCredentialProofResult> proofs)
            throws Throwable;

    /**
     * Find a transaction that is not expired and was started for the client and the user of the access token.
     *
     * @param accessToken   the access token presented at the deferred credential endpoint
     * @param transactionId the transaction id
     * @return the transaction, if any
     */
    Optional<DeferredTransaction> find(OAuth20AccessToken accessToken, String transactionId);

    /**
     * Remove a transaction whose credentials are being handed out or that was denied, so its id cannot be used again.
     * Only one caller removes it, which is how two concurrent requests for an approved transaction are kept from both
     * obtaining its credentials. That holds with ticket registries that report whether a deletion removed anything (the
     * in-memory, Hazelcast, Redis, JPA, MongoDB, DynamoDB and Ignite registries); those that always report one deletion
     * cannot tell concurrent callers apart.
     *
     * @param transactionId the transaction id
     * @return true when this call removed the transaction
     * @throws Throwable the throwable
     */
    boolean complete(String transactionId) throws Throwable;

    /**
     * Transactions that are not expired, optionally for one user only.
     *
     * @param principal the principal identifier, or null for all users
     * @return the transactions
     */
    List<DeferredTransaction> getTransactions(@Nullable String principal);

    /**
     * Approve or deny a transaction.
     *
     * @param transactionId the transaction id
     * @param status        the new status, {@code APPROVED} or {@code DENIED}
     * @return the updated transaction, or empty when it is unknown or expired
     * @throws Throwable the throwable
     */
    Optional<DeferredTransaction> decide(String transactionId, TransactionStatus status) throws Throwable;

    /**
     * Status of a deferred transaction.
     */
    enum TransactionStatus {
        /**
         * Waiting for a decision; the wallet is asked to come back later.
         */
        PENDING,
        /**
         * Approved; the wallet collects the credentials.
         */
        APPROVED,
        /**
         * Denied; the wallet is told the credentials will not be issued.
         */
        DENIED
    }

    /**
     * A deferred transaction. The holder keys are not shown by the actuator endpoint.
     *
     * @param transactionId             the transaction id, used by the wallet as {@code transaction_id}
     * @param clientId                  the client that requested the credentials
     * @param principal                 the user the credentials are for
     * @param credentialConfigurationId the credential configuration id
     * @param status                    the status
     * @param createdAt                 when the transaction started, in seconds since the epoch
     * @param proofs                    the validated proofs, one per holder key
     */
    record DeferredTransaction(
        String transactionId,
        String clientId,
        String principal,
        String credentialConfigurationId,
        TransactionStatus status,
        long createdAt,
        @JsonIgnore List<VerifiableCredentialProofResult> proofs) {

        /**
         * Number of credentials the transaction issues, one per holder key.
         *
         * @return the number of credentials
         */
        public int getCredentials() {
            return proofs.size();
        }
    }
}
