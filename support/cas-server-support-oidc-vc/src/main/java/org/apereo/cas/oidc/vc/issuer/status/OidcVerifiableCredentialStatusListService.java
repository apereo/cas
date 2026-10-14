package org.apereo.cas.oidc.vc.issuer.status;

import module java.base;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Manages the Token Status List of issued credentials (draft-ietf-oauth-status-list): allocates the status list entry a
 * credential references in its {@code status} claim, publishes status list tokens, and reads and changes the status of
 * entries.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public interface OidcVerifiableCredentialStatusListService {
    /**
     * Default bean name.
     */
    String BEAN_NAME = "oidcVerifiableCredentialStatusListService";

    /**
     * Allocate a status list entry for a credential, with a random index that no other unexpired credential uses.
     *
     * @param accessToken               the access token that obtains the credential
     * @param principal                 the subject of the credential
     * @param credentialConfigurationId the credential configuration
     * @param credentialId              the credential identifier, its {@code jti}
     * @param validity                  how long the credential is valid
     * @return the status reference, or empty when status lists are disabled or entries cannot be kept
     * @throws Throwable the throwable
     */
    Optional<StatusReference> allocate(OAuth20AccessToken accessToken, String principal, String credentialConfigurationId,
                                       String credentialId, Duration validity)
            throws Throwable;

    /**
     * Build the signed status list token of a status list.
     *
     * @param statusListId the status list identifier
     * @return the status list token, or empty when the status list has no entries
     * @throws Throwable the throwable
     */
    Optional<String> buildStatusListToken(String statusListId) throws Throwable;

    /**
     * Status of the entry a {@code status_list} reference names, provided it is an unexpired entry of a status list
     * published by CAS.
     *
     * @param uri   the status list uri
     * @param index the index
     * @return the status, or empty when the entry is unknown
     */
    Optional<StatusType> getStatus(String uri, long index);

    /**
     * Status list entries of the credentials issued to a user.
     *
     * @param principal the principal identifier
     * @return the entries
     */
    List<StatusEntry> getEntries(String principal);

    /**
     * URIs of the status lists that hold unexpired entries, published as the status list aggregation
     * (draft-ietf-oauth-status-list, section 9).
     *
     * @return the status list URIs
     */
    List<String> getStatusListUris();

    /**
     * Change the status of an entry.
     *
     * @param statusListId the status list identifier
     * @param index        the index
     * @param status       the new status
     * @return the updated entry, or empty when the entry is unknown
     * @throws Throwable the throwable
     */
    Optional<StatusEntry> updateStatus(String statusListId, long index, StatusType status) throws Throwable;

    /**
     * Status types of draft-ietf-oauth-status-list, section 7.
     */
    @Getter
    @RequiredArgsConstructor
    enum StatusType {
        /**
         * The credential is valid.
         */
        VALID(0),
        /**
         * The credential is revoked.
         */
        INVALID(1),
        /**
         * The credential is temporarily invalid.
         */
        SUSPENDED(2);

        private final int value;
    }

    /**
     * A reference to a status list entry, as carried by a credential.
     *
     * @param uri   the status list uri
     * @param index the index
     */
    record StatusReference(String uri, long index) {
        /**
         * The {@code status} claim of the credential.
         *
         * @return the claim value
         */
        public Map<String, Object> toClaim() {
            return Map.of("status_list", Map.of("idx", index, "uri", uri));
        }
    }

    /**
     * A status list entry.
     *
     * @param statusListId              the status list identifier
     * @param index                     the index
     * @param uri                       the status list uri
     * @param status                    the status
     * @param principal                 the user the credential was issued to
     * @param clientId                  the client that obtained the credential
     * @param credentialConfigurationId the credential configuration
     * @param credentialId              the credential identifier
     * @param expiresAt                 when the credential expires, in epoch seconds
     */
    record StatusEntry(String statusListId, long index, String uri, StatusType status, String principal,
                       String clientId, String credentialConfigurationId, String credentialId, long expiresAt) {
    }
}
