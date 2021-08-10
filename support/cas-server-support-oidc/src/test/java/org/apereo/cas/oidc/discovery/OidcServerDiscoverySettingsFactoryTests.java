package org.apereo.cas.oidc.discovery;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.oidc.AbstractOidcTests;

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
public class OidcServerDiscoverySettingsFactoryTests extends AbstractOidcTests {

    @TestConfiguration("OidcAuthenticationContextTestConfiguration")
    public static class OidcAuthenticationContextTestConfiguration {
        @Bean
        public MultifactorAuthenticationProvider dummyProvider() {
            return new TestMultifactorAuthenticationProvider();
        }
    }

    @Test
    public void verifyAction() {
        assertTrue(oidcServerDiscoverySettings.isRequestParameterSupported());
        assertTrue(oidcServerDiscoverySettings.isClaimsParameterSupported());
        
        assertFalse(oidcServerDiscoverySettings.getClaimsSupported().isEmpty());
        assertFalse(oidcServerDiscoverySettings.getClaimTypesSupported().isEmpty());
        assertFalse(oidcServerDiscoverySettings.getGrantTypesSupported().isEmpty());
        assertFalse(oidcServerDiscoverySettings.getIntrospectionSupportedAuthenticationMethods().isEmpty());
        assertFalse(oidcServerDiscoverySettings.getIdTokenSigningAlgValuesSupported().isEmpty());
        assertFalse(oidcServerDiscoverySettings.getSubjectTypesSupported().isEmpty());
        assertFalse(oidcServerDiscoverySettings.getAcrValuesSupported().isEmpty());

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
}
