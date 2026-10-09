package org.apereo.cas.oidc.discovery;

import module java.base;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20ClientAuthenticationMethods;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcServerDiscoverySettingsFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("OIDC")
@Import(OidcServerDiscoverySettingsFactoryTests.OidcAuthenticationContextTestConfiguration.class)
class OidcServerDiscoverySettingsFactoryTests extends AbstractOidcTests {

    @TestConfiguration(value = "OidcAuthenticationContextTestConfiguration", proxyBeanMethods = false)
    static class OidcAuthenticationContextTestConfiguration {
        @Bean
        public MultifactorAuthenticationProvider dummyProvider() {
            return new TestMultifactorAuthenticationProvider();
        }
    }

    @Test
    void verifyAction() {
        assertTrue(oidcServerDiscoverySettings.isRequestParameterSupported());
        assertTrue(oidcServerDiscoverySettings.isClaimsParameterSupported());
        
        assertFalse(oidcServerDiscoverySettings.getClaimsSupported().isEmpty());
        assertFalse(oidcServerDiscoverySettings.getClaimTypesSupported().isEmpty());
        assertFalse(oidcServerDiscoverySettings.getGrantTypesSupported().isEmpty());
        assertFalse(oidcServerDiscoverySettings.getIntrospectionSupportedAuthenticationMethods().isEmpty());
        assertFalse(oidcServerDiscoverySettings.getIdTokenSigningAlgValuesSupported().isEmpty());
        assertFalse(oidcServerDiscoverySettings.getSubjectTypesSupported().isEmpty());
        assertFalse(oidcServerDiscoverySettings.getAcrValuesSupported().isEmpty());
        assertFalse(oidcServerDiscoverySettings.getDPopSigningAlgValuesSupported().isEmpty());

        assertFalse(oidcServerDiscoverySettings.getResponseTypesSupported().isEmpty());
        assertFalse(oidcServerDiscoverySettings.getScopesSupported().isEmpty());

        assertNotNull(oidcServerDiscoverySettings.getEndSessionEndpoint());
        assertNotNull(oidcServerDiscoverySettings.getIntrospectionEndpoint());
        assertNotNull(oidcServerDiscoverySettings.getRegistrationEndpoint());
        assertNotNull(oidcServerDiscoverySettings.getTokenEndpoint());
        assertNotNull(oidcServerDiscoverySettings.getUserinfoEndpoint());
        assertNotNull(oidcServerDiscoverySettings.getIssuer());
        assertNotNull(oidcServerDiscoverySettings.getJwksUri());
    }

    @Test
    void verifyClientAttestationIsAdvertisedWithTrustAnchors() throws Exception {
        assertFalse(oidcServerDiscoverySettings.getTokenEndpointAuthMethodsSupported()
            .contains(OAuth20ClientAuthenticationMethods.ATTEST_JWT_CLIENT_AUTH.getType()));
        assertNull(oidcServerDiscoverySettings.getClientAttestationSigningAlgValuesSupported());

        val properties = new CasConfigurationProperties();
        properties.getAuthn().getOidc().getClientAttestation().getTrustAnchors().add("classpath:client-attestation-root.pem");
        val settings = new OidcServerDiscoverySettingsFactory(properties, oidcIssuerService, applicationContext).getObject();
        assertTrue(settings.getTokenEndpointAuthMethodsSupported().contains(OAuth20ClientAuthenticationMethods.ATTEST_JWT_CLIENT_AUTH.getType()));
        assertTrue(settings.getClientAttestationSigningAlgValuesSupported().contains("ES256"));
        assertTrue(settings.getClientAttestationPopSigningAlgValuesSupported().contains("ES256"));
        assertTrue(settings.toJson().contains("client_attestation_pop_signing_alg_values_supported"));
        assertTrue(settings.getTokenEndpointAuthMethodsSupported().contains(OAuth20ClientAuthenticationMethods.ATTEST_JWT_CLIENT_AUTH_DPOP.getType()));
        assertNull(settings.getChallengeEndpoint());
        assertNull(settings.getStatusListAggregationEndpoint());
        assertFalse(settings.toJson().contains("challenge_endpoint"));

        properties.getAuthn().getOidc().getClientAttestation().getChallenge().setEnabled(true);
        properties.getAuthn().getOidc().getVc().getIssuer().getStatusList().setEnabled(true);
        val withChallenges = new OidcServerDiscoverySettingsFactory(properties, oidcIssuerService, applicationContext).getObject();
        assertTrue(withChallenges.getChallengeEndpoint().endsWith('/' + OidcConstants.CLIENT_ATTESTATION_CHALLENGE_URL));
        assertTrue(withChallenges.getStatusListAggregationEndpoint().endsWith('/' + OidcConstants.VC_STATUS_LIST_AGGREGATION_URL));
    }

    @Test
    void verifyCredentialConfigurationScopesAreSupported() {
        val properties = new CasConfigurationProperties();
        val configuration = new OidcVerifiableCredentialConfigurationProperties();
        configuration.setScope("UniversityDegree");
        properties.getAuthn().getOidc().getVc().getIssuer().getCredentialConfigurations().put("degree", configuration);
        properties.getAuthn().getOidc().getVc().getIssuer().getCredentialConfigurations()
            .put("unscoped", new OidcVerifiableCredentialConfigurationProperties());

        val scopes = OidcServerDiscoverySettingsFactory.resolveScopesSupported(properties);
        assertTrue(scopes.containsAll(properties.getAuthn().getOidc().getDiscovery().getScopes()));
        assertTrue(scopes.contains("UniversityDegree"));
        assertEquals(properties.getAuthn().getOidc().getDiscovery().getScopes().size() + 1, scopes.size());
    }

    @Test
    void verifyPreAuthorizedGrantAnonymousAccessFollowsGrantType() {
        assertTrue(oidcServerDiscoverySettings.isPreAuthorizedGrantAnonymousAccessSupported());
        val settings = new OidcServerDiscoverySettings(oidcServerDiscoverySettings.getIssuer());
        settings.setGrantTypesSupported(Set.of(OAuth20GrantTypes.AUTHORIZATION_CODE.getType()));
        assertFalse(settings.isPreAuthorizedGrantAnonymousAccessSupported());
    }
}
