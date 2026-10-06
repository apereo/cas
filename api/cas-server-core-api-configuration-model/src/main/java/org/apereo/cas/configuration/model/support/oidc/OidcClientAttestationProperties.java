package org.apereo.cas.configuration.model.support.oidc;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Attestation-based client authentication settings ({@code attest_jwt_client_auth}, and {@code attest_jwt_client_auth_dpop}
 * where a DPoP proof serves as the proof of possession), with which wallets authenticate by a wallet attestation
 * (OpenID4VCI 1.0 Appendix E).
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequiresModule(name = "cas-server-support-oidc")
@Getter
@Setter
@Accessors(chain = true)
public class OidcClientAttestationProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 6392761937011560478L;

    /**
     * Locations of PEM-encoded certificates of the trust anchors that client attestations must chain to, such as those
     * of wallet providers. A client attestation carries its signing certificate, and any intermediate certificates, in
     * its {@code x5c} header, which must lead to one of these anchors. Attestation-based client authentication is not
     * accepted, nor advertised, until at least one trust anchor is configured.
     */
    private List<String> trustAnchors = new ArrayList<>();

    /**
     * Signing algorithms accepted for client attestations and their proofs of possession, advertised as
     * {@code client_attestation_signing_alg_values_supported} and {@code client_attestation_pop_signing_alg_values_supported}.
     * Only the EC ({@code ES*}) and RSA ({@code RS*}, {@code PS*}) algorithms can be verified.
     */
    private List<String> signingAlgValuesSupported = Stream.of("ES256", "ES384", "ES512",
        "PS256", "PS384", "PS512", "RS256", "RS384", "RS512").toList();

    /**
     * Server-provided challenges for client attestation proofs of possession.
     */
    @NestedConfigurationProperty
    private OidcClientAttestationChallengeProperties challenge = new OidcClientAttestationChallengeProperties();
}
