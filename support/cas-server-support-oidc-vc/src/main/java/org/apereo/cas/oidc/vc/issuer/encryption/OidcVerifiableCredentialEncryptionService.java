package org.apereo.cas.oidc.vc.issuer.encryption;

import module java.base;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialRequest;
import org.apereo.cas.oidc.vc.issuer.metadata.OidcCredentialIssuerMetadata;
import org.jspecify.annotations.Nullable;

/**
 * Encryption of credential requests and responses at the credential endpoint, on top of TLS, per OpenID4VCI 1.0
 * section 10. Encrypted messages are JWTs carried in a JWE, sent as {@code application/jwt}. A wallet encrypts its
 * request to a key from {@code credential_request_encryption.jwks} and asks for an encrypted response with
 * {@code credential_response_encryption}; error responses are never encrypted.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public interface OidcVerifiableCredentialEncryptionService {
    /**
     * Default bean name.
     */
    String BEAN_NAME = "oidcVerifiableCredentialEncryptionService";

    /**
     * Whether encryption is turned on and there is a key wallets can encrypt requests to. Without such a key an
     * encrypted response cannot be offered either, since a request asking for one must be encrypted.
     *
     * @return true when enabled
     */
    boolean isEnabled();

    /**
     * Whether every credential request must be encrypted.
     *
     * @return true when required
     */
    boolean isRequestEncryptionRequired();

    /**
     * Whether every credential response must be encrypted.
     *
     * @return true when required
     */
    boolean isResponseEncryptionRequired();

    /**
     * The {@code credential_request_encryption} issuer metadata.
     *
     * @return the metadata, or null when encryption is not enabled
     */
    OidcCredentialIssuerMetadata.@Nullable CredentialRequestEncryption buildRequestEncryptionMetadata();

    /**
     * The {@code credential_response_encryption} issuer metadata.
     *
     * @return the metadata, or null when encryption is not enabled
     */
    OidcCredentialIssuerMetadata.@Nullable CredentialResponseEncryption buildResponseEncryptionMetadata();

    /**
     * Decrypt an encrypted credential request.
     *
     * @param request the compact JWE
     * @return the decrypted request, as JSON
     * @throws OidcVerifiableCredentialEncryptionException when the request cannot be decrypted
     */
    String decryptRequest(String request);

    /**
     * Check the parameters a wallet supplied for encrypting the credential response, before anything is issued.
     *
     * @param parameters the parameters
     * @throws OidcVerifiableCredentialEncryptionException when the parameters cannot be used
     */
    void validateResponseEncryption(OidcVerifiableCredentialRequest.CredentialResponseEncryption parameters);

    /**
     * Encrypt a credential response.
     *
     * @param response   the response, as JSON
     * @param parameters the parameters the wallet supplied
     * @return the compact JWE
     * @throws OidcVerifiableCredentialEncryptionException when the response cannot be encrypted
     */
    String encryptResponse(String response, OidcVerifiableCredentialRequest.CredentialResponseEncryption parameters);
}
