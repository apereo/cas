package org.apereo.cas.configuration.model.support.oidc;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Encryption of credential requests and responses at the credential endpoint, on top of TLS, per OpenID4VCI 1.0
 * section 10. A wallet that asks for an encrypted response must also encrypt its request; requests are encrypted to
 * the current encryption keys of the CAS OpenID Connect keystore.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiresModule(name = "cas-server-support-oidc-vc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcVerifiableCredentialEncryptionProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 2875190427381564930L;

    /**
     * Whether the issuer metadata advertises {@code credential_request_encryption} and {@code credential_response_encryption},
     * and the credential endpoint accepts encrypted requests and encrypts responses when asked to. The current encryption
     * keys of the OpenID Connect keystore are published for request encryption: an RSA key is used with {@code RSA-OAEP-256}
     * and an elliptic curve key with {@code ECDH-ES}.
     */
    private boolean enabled;

    /**
     * Whether every credential request must be encrypted.
     */
    private boolean requestEncryptionRequired;

    /**
     * Whether every credential response must be encrypted, so that a wallet has to supply
     * {@code credential_response_encryption} with each request.
     */
    private boolean responseEncryptionRequired;

    /**
     * JWE key management algorithms ({@code alg}) the issuer accepts in the key a wallet supplies for response encryption.
     */
    private List<String> algValuesSupported = Stream.of("ECDH-ES", "RSA-OAEP-256").toList();

    /**
     * JWE content encryption algorithms ({@code enc}) accepted for encrypted requests and offered for encrypted responses.
     */
    private List<String> encValuesSupported = Stream.of("A128GCM", "A256GCM").toList();
}
