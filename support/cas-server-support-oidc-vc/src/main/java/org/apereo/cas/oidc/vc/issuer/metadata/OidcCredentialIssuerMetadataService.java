package org.apereo.cas.oidc.vc.issuer.metadata;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialsIssuerProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.vc.issuer.enc.BaseOidcVerifiableCredentialEncoder;
import org.apereo.cas.oidc.vc.issuer.encryption.OidcVerifiableCredentialEncryptionService;
import org.apereo.cas.oidc.vc.issuer.metadata.CredentialConfigurationDisplay.CredentialConfigurationDisplayLogo;
import org.apereo.cas.oidc.vc.issuer.metadata.OidcCredentialConfigurationTypeMetadata.ClaimMetadata;
import org.apereo.cas.oidc.vc.issuer.metadata.OidcCredentialIssuerMetadata.ClaimMetadata.ClaimDisplay;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;


/**
 * This is {@link OidcCredentialIssuerMetadataService}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
public class OidcCredentialIssuerMetadataService {
    private final CasConfigurationProperties casProperties;

    private final OidcVerifiableCredentialEncryptionService encryptionService;

    /**
     * Build oidc credential issuer metadata.
     *
     * @return the oidc credential issuer metadata
     */
    public OidcCredentialIssuerMetadata build() {
        val properties = casProperties.getAuthn().getOidc();
        val issuer = properties.getCore().getIssuer();

        val metadata = new OidcCredentialIssuerMetadata();
        metadata.setCredentialIssuer(issuer);
        metadata.setAuthorizationServers(List.of(issuer));
        metadata.setCredentialEndpoint(issuer + '/' + OidcConstants.VC_CREDENTIAL_URL);
        metadata.setNonceEndpoint(issuer + '/' + OidcConstants.VC_NONCE_URL);
        metadata.setNotificationEndpoint(issuer + '/' + OidcConstants.VC_NOTIFICATION_URL);
        metadata.setCredentialRequestEncryption(encryptionService.buildRequestEncryptionMetadata());
        metadata.setCredentialResponseEncryption(encryptionService.buildResponseEncryptionMetadata());
        metadata.setDisplay(buildIssuerDisplays(properties.getVc().getIssuer().getDisplay()));
        metadata.setBatchCredentialIssuance(OidcCredentialIssuerMetadata.BatchCredentialIssuance
            .builder()
            .batchSize(Math.max(1, properties.getVc().getIssuer().getBatchSize()))
            .build());

        val supported = new LinkedHashMap<String, OidcCredentialIssuerMetadata.CredentialConfiguration>();
        val credentialConfigurations = properties.getVc().getIssuer().getCredentialConfigurations();
        credentialConfigurations.forEach((key, value) -> {
            val cfg = new OidcCredentialIssuerMetadata.CredentialConfiguration();
            cfg.setFormat(value.getFormat().getValue());
            cfg.setScope(value.getScope());
            if (value.getFormat() == OidcVerifiableCredentialConfigurationProperties.CredentialConfigurationFormats.DC_SD_JWT) {
                cfg.setVct(issuer + '/' + OidcConstants.VC_CREDENTIAL_TYPE_URL + '/' + key);
            } else {
                cfg.setCredentialDefinition(OidcCredentialIssuerMetadata.CredentialDefinition
                    .builder()
                    .context(value.getFormat() == OidcVerifiableCredentialConfigurationProperties.CredentialConfigurationFormats.JWT_VC_JSON_LD
                        ? List.of(BaseOidcVerifiableCredentialEncoder.VCDM_V2_CONTEXT)
                        : null)
                    .type(BaseOidcVerifiableCredentialEncoder.resolveCredentialTypes(key, value))
                    .build());
            }
            cfg.setCryptographicBindingMethodsSupported(value.getCryptographicBindingMethodsSupported());
            cfg.setCredentialSigningAlgValuesSupported(value.getCredentialSigningAlgValuesSupported());

            cfg.setProofTypesSupported(buildProofTypesSupported(value));

            if (properties.getVc().getMetadata().isIncludeClaims()) {
                val claims = new ArrayList<OidcCredentialIssuerMetadata.ClaimMetadata>();
                value.getClaims().forEach((claimName, claimProps) -> {
                    val claim = new OidcCredentialIssuerMetadata.ClaimMetadata();
                    claim.setPath(List.of(claimName));
                    val displays = claimProps.getDisplay()
                        .stream()
                        .<ClaimDisplay>map(entry -> ClaimDisplay
                            .builder()
                            .locale(entry.getLocale())
                            .name(StringUtils.defaultIfBlank(entry.getName(), claimName))
                            .build())
                        .toList();
                    claim.setDisplay(displays);
                    claims.add(claim);
                });
                cfg.getCredentialMetadata().setClaims(claims);
            }

            val displays = buildCredentialConfigurationDisplays(value);
            cfg.getCredentialMetadata().setDisplay(displays);
            supported.put(key, cfg);
        });

        metadata.setCredentialConfigurationsSupported(supported);
        return metadata;
    }

    private static List<CredentialConfigurationDisplay> buildIssuerDisplays(
        final List<OidcVerifiableCredentialsIssuerProperties.IssuerDisplay> issuerDisplays) {
        return issuerDisplays
            .stream()
            .map(entry -> {
                val display = new CredentialConfigurationDisplay();
                display.setName(entry.getName());
                display.setLocale(entry.getLocale());
                if (StringUtils.isNotBlank(entry.getLogo())) {
                    display.setLogo(CredentialConfigurationDisplayLogo.builder()
                        .uri(entry.getLogo())
                        .altText(StringUtils.defaultIfBlank(entry.getLogoAltText(), entry.getName()))
                        .build());
                }
                return display;
            })
            .toList();
    }

    private static List<CredentialConfigurationDisplay> buildCredentialConfigurationDisplays(
        final OidcVerifiableCredentialConfigurationProperties value) {
        return value.getDisplay()
            .stream()
            .map(entry -> {
                val display = new CredentialConfigurationDisplay();
                display.setName(entry.getName());
                display.setLocale(entry.getLocale());
                display.setDescription(entry.getDescription());
                display.setLogo(CredentialConfigurationDisplayLogo.builder()
                    .altText(entry.getName())
                    .uri(entry.getLogo())
                    .build());
                display.setBackgroundColor(entry.getBackgroundColor());
                display.setTextColor(entry.getTextColor());
                return display;
            }).toList();
    }

    /**
     * Describe configuration metadata.
     *
     * @param configurationId the configuration id
     * @return the oidc credential configuration type metadata
     */
    public @Nullable OidcCredentialConfigurationTypeMetadata describeConfiguration(final String configurationId) {
        val properties = casProperties.getAuthn().getOidc();
        val configuration = properties.getVc().getIssuer().getCredentialConfigurations().get(configurationId);
        if (configuration != null) {
            val metadata = new OidcCredentialConfigurationTypeMetadata();
            val issuer = properties.getCore().getIssuer();
            metadata.setVct(issuer + '/' + OidcConstants.VC_CREDENTIAL_TYPE_URL + '/' + configurationId);
            val display = configuration.getDisplay().getFirst();
            metadata.setName(display.getName());
            metadata.setDescription(display.getDescription());

            val displays = buildCredentialConfigurationDisplays(configuration);
            metadata.setDisplay(displays);

            val claims = configuration.getClaims()
                .entrySet()
                .stream()
                .<ClaimMetadata>map(entry -> {
                    val claimDisplay = entry.getValue()
                        .getDisplay()
                        .stream()
                        .<OidcCredentialConfigurationTypeMetadata.ClaimDisplay>map(dp ->
                            OidcCredentialConfigurationTypeMetadata.ClaimDisplay
                                .builder()
                                .lang(dp.getLocale())
                                .label(StringUtils.defaultIfBlank(dp.getName(), entry.getKey()))
                                .build())
                        .toList();
                    return ClaimMetadata
                        .builder()
                        .path(List.of(entry.getKey()))
                        .sd(entry.getValue().isDisclosable() ? "allowed" : StringUtils.EMPTY)
                        .display(claimDisplay)
                        .build();
                })
                .toList();
            metadata.setClaims(claims);
            return metadata;
        }
        return null;
    }

    /**
     * Proof types a credential configuration accepts. A {@code jwt} proof always; it lists
     * {@code key_attestations_required} when the configuration requires key attestations. The {@code attestation}
     * proof type (OpenID4VCI 1.0 Appendix D) is offered once key attestation trust anchors are configured, and always
     * lists {@code key_attestations_required}, since the proof is a key attestation.
     *
     * @param configuration the credential configuration
     * @return the proof types supported
     */
    protected Map<String, OidcCredentialIssuerMetadata.ProofTypeSupported> buildProofTypesSupported(
        final OidcVerifiableCredentialConfigurationProperties configuration) {
        val requirement = configuration.getKeyAttestations();
        final OidcCredentialIssuerMetadata.KeyAttestationsRequired keyAttestationsRequired = OidcCredentialIssuerMetadata.KeyAttestationsRequired.builder()
            .keyStorage(requirement.getKeyStorage().isEmpty() ? null : requirement.getKeyStorage())
            .userAuthentication(requirement.getUserAuthentication().isEmpty() ? null : requirement.getUserAuthentication())
            .build();
        val proofTypes = new LinkedHashMap<String, OidcCredentialIssuerMetadata.ProofTypeSupported>();
        val jwt = new OidcCredentialIssuerMetadata.ProofTypeSupported();
        jwt.setProofSigningAlgValuesSupported(configuration.getProofSigningAlgValuesSupported());
        if (requirement.isRequired()) {
            jwt.setKeyAttestationsRequired(keyAttestationsRequired);
        }
        proofTypes.put("jwt", jwt);
        if (!casProperties.getAuthn().getOidc().getVc().getIssuer().getKeyAttestation().getTrustAnchors().isEmpty()) {
            val attestation = new OidcCredentialIssuerMetadata.ProofTypeSupported();
            attestation.setProofSigningAlgValuesSupported(configuration.getProofSigningAlgValuesSupported());
            attestation.setKeyAttestationsRequired(keyAttestationsRequired);
            proofTypes.put("attestation", attestation);
        }
        return proofTypes;
    }
}
