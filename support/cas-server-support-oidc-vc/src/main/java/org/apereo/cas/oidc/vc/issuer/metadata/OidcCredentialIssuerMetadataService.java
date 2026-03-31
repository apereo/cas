package org.apereo.cas.oidc.vc.issuer.metadata;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link OidcCredentialIssuerMetadataService}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
public class OidcCredentialIssuerMetadataService {
    private final CasConfigurationProperties casProperties;

    /**
     * Build oidc credential issuer metadata.
     *
     * @return the oidc credential issuer metadata
     */
    public OidcCredentialIssuerMetadata build() {
        val properties = casProperties.getAuthn().getOidc();
        
        val metadata = new OidcCredentialIssuerMetadata();
        metadata.setCredentialIssuer(properties.getCore().getIssuer());
        metadata.setAuthorizationServers(List.of(properties.getCore().getIssuer()));
        metadata.setCredentialEndpoint(properties.getCore().getIssuer()
            + '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_CREDENTIAL_URL);
        metadata.setNonceEndpoint(properties.getCore().getIssuer()
            + '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.VC_NONCE_URL);

        val supported = new LinkedHashMap<String, OidcCredentialIssuerMetadata.CredentialConfiguration>();
        properties.getVc().getIssuer().getCredentialConfigurations().forEach((key, value) -> {
            val cfg = new OidcCredentialIssuerMetadata.CredentialConfiguration();
            cfg.setFormat(value.getFormat());
            cfg.setScope(value.getScope());
            cfg.setCryptographicBindingMethodsSupported(value.getCryptographicBindingMethodsSupported());
            cfg.setCredentialSigningAlgValuesSupported(value.getCredentialSigningAlgValuesSupported());

            val proof = new OidcCredentialIssuerMetadata.ProofTypeSupported();
            proof.setProofSigningAlgValuesSupported(value.getProofSigningAlgValuesSupported());
            cfg.setProofTypesSupported(Map.of("jwt", proof));

            val claims = new LinkedHashMap<String, OidcCredentialIssuerMetadata.ClaimMetadata>();
            value.getClaims().forEach((claimName, claimProps) -> {
                val claim = new OidcCredentialIssuerMetadata.ClaimMetadata();
                claim.setMandatory(claimProps.isMandatory());
                claim.setValueType(claimProps.getValueType());
                claims.put(claimName, claim);
            });
            cfg.setClaims(claims);
            supported.put(key, cfg);
        });

        metadata.setCredentialConfigurationsSupported(supported);
        return metadata;
    }
}
