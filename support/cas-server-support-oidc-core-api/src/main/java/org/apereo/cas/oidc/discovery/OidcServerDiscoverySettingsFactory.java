package org.apereo.cas.oidc.discovery;

import module java.base;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.support.oauth.OAuth20ClientAuthenticationMethods;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link OidcServerDiscoverySettingsFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class OidcServerDiscoverySettingsFactory implements FactoryBean<OidcServerDiscoverySettings> {
    protected final CasConfigurationProperties casProperties;

    protected final OidcIssuerService issuerService;

    protected final ConfigurableApplicationContext applicationContext;

    @Override
    public OidcServerDiscoverySettings getObject() {
        return populateDiscovery(createDiscovery());
    }

    protected OidcServerDiscoverySettings createDiscovery() {
        return new OidcServerDiscoverySettings(issuerService.determineIssuer(Optional.empty()));
    }

    protected OidcServerDiscoverySettings populateDiscovery(final OidcServerDiscoverySettings discovery) {
        val oidc = casProperties.getAuthn().getOidc();
        val discoveryConfig = oidc.getDiscovery();

        discovery.setClaimsSupported(new LinkedHashSet<>(discoveryConfig.getClaims()));
        discovery.setScopesSupported(new LinkedHashSet<>(resolveScopesSupported(casProperties)));
        discovery.setResponseTypesSupported(new LinkedHashSet<>(discoveryConfig.getResponseTypesSupported()));
        discovery.setResponseModesSupported(new LinkedHashSet<>(discoveryConfig.getResponseModesSupported()));
        discovery.setSubjectTypesSupported(new LinkedHashSet<>(discoveryConfig.getSubjectTypes()));
        discovery.setClaimTypesSupported(new LinkedHashSet<>(discoveryConfig.getClaimTypesSupported()));
        discovery.setIntrospectionSupportedAuthenticationMethods(
            new LinkedHashSet<>(discoveryConfig.getIntrospectionSupportedAuthenticationMethods()));
        discovery.setGrantTypesSupported(new LinkedHashSet<>(discoveryConfig.getGrantTypesSupported()));
        discovery.setTokenEndpointAuthMethodsSupported(
            new LinkedHashSet<>(discoveryConfig.getTokenEndpointAuthMethodsSupported()));
        discovery.setTokenEndpointAuthSigningAlgValuesSupported(
            new LinkedHashSet<>(discoveryConfig.getTokenEndpointAuthSigningAlgValuesSupported()));
        val clientAttestation = oidc.getClientAttestation();
        if (!clientAttestation.getTrustAnchors().isEmpty()) {
            discovery.getTokenEndpointAuthMethodsSupported().add(OAuth20ClientAuthenticationMethods.ATTEST_JWT_CLIENT_AUTH.getType());
            discovery.setClientAttestationSigningAlgValuesSupported(new LinkedHashSet<>(clientAttestation.getSigningAlgValuesSupported()));
            discovery.setClientAttestationPopSigningAlgValuesSupported(new LinkedHashSet<>(clientAttestation.getSigningAlgValuesSupported()));
        }
        discovery.setClaimsParameterSupported(discoveryConfig.isClaimsParameterSupported());
        discovery.setPromptValuesSupported(new LinkedHashSet<>(discoveryConfig.getPromptValuesSupported()));

        discovery.setIdTokenSigningAlgValuesSupported(
            new LinkedHashSet<>(discoveryConfig.getIdTokenSigningAlgValuesSupported()));
        discovery.setIdTokenEncryptionAlgValuesSupported(
            new LinkedHashSet<>(discoveryConfig.getIdTokenEncryptionAlgValuesSupported()));
        discovery.setIdTokenEncryptionEncodingValuesSupported(
            new LinkedHashSet<>(discoveryConfig.getIdTokenEncryptionEncodingValuesSupported()));

        discovery.setIntrospectionSignedResponseAlgValuesSupported(
            new LinkedHashSet<>(discoveryConfig.getIntrospectionSignedResponseAlgValuesSupported()));
        discovery.setIntrospectionEncryptedResponseAlgValuesSupported(
            new LinkedHashSet<>(discoveryConfig.getIntrospectionEncryptedResponseAlgValuesSupported()));
        discovery.setIntrospectionEncryptedResponseEncodingValuesSupported(
            new LinkedHashSet<>(discoveryConfig.getIntrospectionEncryptedResponseEncodingValuesSupported()));

        discovery.setBackchannelLogoutSupported(oidc.getLogout().isBackchannelLogoutSupported());
        discovery.setFrontchannelLogoutSupported(oidc.getLogout().isFrontchannelLogoutSupported());

        discovery.setDPopSigningAlgValuesSupported(
            new LinkedHashSet<>(discoveryConfig.getDpopSigningAlgValuesSupported()));

        discovery.setUserInfoSigningAlgValuesSupported(
            new LinkedHashSet<>(discoveryConfig.getUserInfoSigningAlgValuesSupported()));
        discovery.setUserInfoEncryptionAlgValuesSupported(
            new LinkedHashSet<>(discoveryConfig.getUserInfoEncryptionAlgValuesSupported()));
        discovery.setUserInfoEncryptionEncodingValuesSupported(
            new LinkedHashSet<>(discoveryConfig.getUserInfoEncryptionEncodingValuesSupported()));
        discovery.setCodeChallengeMethodsSupported(
            new LinkedHashSet<>(discoveryConfig.getCodeChallengeMethodsSupported()));

        discovery.setRequirePushedAuthorizationRequests(discoveryConfig.isRequirePushedAuthorizationRequests());
        discovery.setNativeSsoSupported(discoveryConfig.isNativeSsoSupported());
        discovery.setRequestParameterSupported(discoveryConfig.isRequestParameterSupported());
        discovery.setRequestUriParameterSupported(discoveryConfig.isRequestUriParameterSupported());
        discovery.setRequestObjectSigningAlgValuesSupported(new LinkedHashSet<>(discoveryConfig.getRequestObjectSigningAlgValuesSupported()));
        discovery.setRequestObjectEncryptionAlgValuesSupported(new LinkedHashSet<>(discoveryConfig.getRequestObjectEncryptionAlgValuesSupported()));
        discovery.setRequestObjectEncryptionEncodingValuesSupported(
            new LinkedHashSet<>(discoveryConfig.getRequestObjectEncryptionEncodingValuesSupported()));

        discovery.setAuthorizationResponseIssuerParameterSupported(discoveryConfig.isAuthorizationResponseIssuerParameterSupported());
        discovery.setTlsClientCertificateBoundAccessTokens(discoveryConfig.isTlsClientCertificateBoundAccessTokens());

        discovery.setAcrValuesSupported(new LinkedHashSet<>(discoveryConfig.getAcrValuesSupported()));
        if (discoveryConfig.getAcrValuesSupported().isEmpty()) {
            val providers = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext)
                .values()
                .stream()
                .map(MultifactorAuthenticationProvider::getId)
                .collect(Collectors.toSet());
            discovery.setAcrValuesSupported(providers);
        }

        discovery.setVerifiedClaimsSupported(discoveryConfig.isVerifiedClaimsSupported());
        discovery.setTrustFrameworksSupported(discoveryConfig.getTrustFrameworksSupported());
        discovery.setEvidenceSupported(discoveryConfig.getEvidenceSupported());
        discovery.setDocumentsSupported(discoveryConfig.getDocumentsSupported());
        discovery.setDocumentsValidationMethodsSupported(discoveryConfig.getDocumentsValidationMethodsSupported());
        discovery.setDocumentsVerificationMethodsSupported(discoveryConfig.getDocumentsVerificationMethodsSupported());
        discovery.setElectronicRecordsSupported(discoveryConfig.getElectronicRecordsSupported());
        discovery.setClaimsInVerifiedClaimsSupported(discoveryConfig.getClaimsInVerifiedClaimsSupported());
        
        discovery.setBackchannelUserCodeParameterSupported(discoveryConfig.isBackchannelUserCodeParameterSupported());
        discovery.setBackchannelTokenDeliveryModesSupported(new LinkedHashSet<>(discoveryConfig.getBackchannelTokenDeliveryModesSupported()));
        discovery.setBackchannelAuthenticationRequestSigningAlgValuesSupported(new LinkedHashSet<>(discoveryConfig.getBackchannelAuthenticationRequestSigningAlgValuesSupported()));

        return discovery;
    }

    /**
     * Scopes CAS supports: the configured discovery scopes, plus the {@code scope} of every verifiable credential
     * configuration. OpenID4VCI 1.0 section 5.1.2 lets a wallet request a credential by that scope instead of
     * authorization details, so the scope is advertised and kept when an authorization request carries it, rather
     * than having to be repeated among the discovery scopes.
     *
     * @param casProperties the CAS properties
     * @return the supported scopes
     */
    public static List<String> resolveScopesSupported(final CasConfigurationProperties casProperties) {
        val oidc = casProperties.getAuthn().getOidc();
        val credentialScopes = oidc.getVc().getIssuer().getCredentialConfigurations()
            .values()
            .stream()
            .map(OidcVerifiableCredentialConfigurationProperties::getScope)
            .filter(StringUtils::isNotBlank);
        return Stream.concat(oidc.getDiscovery().getScopes().stream(), credentialScopes).distinct().toList();
    }

    @Override
    public Class<?> getObjectType() {
        return OidcServerDiscoverySettings.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
