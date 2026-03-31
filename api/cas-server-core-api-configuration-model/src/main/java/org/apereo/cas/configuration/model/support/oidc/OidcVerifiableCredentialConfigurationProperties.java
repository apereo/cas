package org.apereo.cas.configuration.model.support.oidc;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link OidcVerifiableCredentialConfigurationProperties}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-oidc-vc")
public class OidcVerifiableCredentialConfigurationProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 6236647635085072105L;

    /**
     * Defines the credential format issued for this configuration.
     * Typical values depend on the verifiable credential profile supported
     * by the issuer, such as JWT-based or SD-JWT-based credentials.
     * This value is published in issuer metadata and is used by wallets
     * to determine how the credential request and response should be processed.
     */
    private String format;

    /**
     * OAuth scope associated with this credential configuration.
     * This scope may be used during authorization and token issuance
     * to indicate that the client or wallet is requesting permission
     * to obtain this specific type of verifiable credential.
     */
    private String scope;

    /**
     * Lists the supported cryptographic binding methods for this credential.
     * These values indicate how the issued credential may be bound to key material
     * controlled by the wallet or holder.
     * A common value is {@code jwk}, which indicates binding via a JSON Web Key.
     */
    private List<String> cryptographicBindingMethodsSupported = Stream.of("jwk").toList();

    /**
     * Lists the signing algorithms supported by the issuer when producing
     * the credential for this configuration.
     * These algorithm identifiers are typically JOSE signature algorithms
     * such as {@code ES256} or {@code RS256}, and are advertised in issuer metadata
     * so wallets know what credential signature formats are supported.
     */
    private List<String> credentialSigningAlgValuesSupported = Stream.of("ES256", "RS256").toList();

    /**
     * Lists the signing algorithms supported for proof validation when the wallet
     * submits proof material as part of a credential request.
     * These values represent the algorithms CAS accepts when verifying
     * proof-of-possession tokens or JWT-based proofs presented by the holder.
     */
    private List<String> proofSigningAlgValuesSupported = Stream.of("ES256", "RS256").toList();

    /**
     * Collection of claim definitions supported by this credential configuration.
     * The map key is the logical claim name that will appear in the issued credential,
     * while the value describes how that claim is sourced, typed, and enforced.
     * Claim definitions are typically used to map resolved principal attributes
     * into the final credential payload.
     */
    private Map<String, OidcVerifiableCredentialClaimProperties> claims = new LinkedHashMap<>();
}
