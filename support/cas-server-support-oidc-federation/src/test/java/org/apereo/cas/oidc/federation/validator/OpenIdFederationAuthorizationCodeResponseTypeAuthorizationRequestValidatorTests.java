package org.apereo.cas.oidc.federation.validator;

import module java.base;
import org.apereo.cas.oidc.federation.AbstractOidcOpenIdProviderFederationTests;
import org.apereo.cas.oidc.federation.chain.OidcFederationDefaultTrustChainResolver;
import org.apereo.cas.oidc.federation.chain.OidcFederationTrustChainResolver;
import org.apereo.cas.services.DefaultRegisteredServiceExpirationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationRequestValidator;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OpenIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidatorTests}.
 *
 * @author Jerome LELEU
 * @since 8.1.0
 */
@Tag("OIDCWeb")
@Import(OpenIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidatorTests.OpenIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidatorTestConfiguration.class)
class OpenIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidatorTests extends AbstractOidcOpenIdProviderFederationTests {
    @Autowired
    @Qualifier("openIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidator")
    private OAuth20AuthorizationRequestValidator authorizationRequestValidator;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;

    @Test
    void verifyMissingServiceGetsResolvedAndSaved() throws Throwable {
        clearOAuthRegisteredServices();
        val clientId = "https://rp-missing-" + UUID.randomUUID() + ".example.org";

        val registeredService = resolveByClientId(clientId);
        assertNotNull(registeredService);
        assertEquals(clientId, registeredService.getClientId());
        assertEquals("resolved-service", registeredService.getName());
        assertTrue(servicesManager.getAllServicesOfType(OAuthRegisteredService.class)
            .stream()
            .anyMatch(service -> service.getClientId().equals(clientId)));
    }

    @Test
    void verifyMissingServiceWithBlankClientIdIsIgnored() throws Throwable {
        clearOAuthRegisteredServices();
        val registeredService = resolveByClientId("   ");
        assertNull(registeredService);
        assertFalse(servicesManager.getAllServicesOfType(OAuthRegisteredService.class)
            .stream()
            .anyMatch(service -> service.getClientId().isBlank()));
    }

    @Test
    void verifyMissingServiceWhenResolverReturnsNoServiceIsIgnored() throws Throwable {
        clearOAuthRegisteredServices();
        val clientId = "https://rp-unresolved-" + UUID.randomUUID() + ".example.org";
        val registeredService = resolveByClientId(clientId);
        assertNull(registeredService);
        assertFalse(servicesManager.getAllServicesOfType(OAuthRegisteredService.class)
            .stream()
            .anyMatch(service -> service.getClientId().equals(clientId)));
    }

    @Test
    void verifyTemporaryServiceNearExpirationGetsRefreshed() throws Throwable {
        clearOAuthRegisteredServices();
        val clientId = "https://rp-refresh-" + UUID.randomUUID() + ".example.org";
        val existingService = buildOidcRegisteredService(clientId, "old-service");
        existingService.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(true,
            ZonedDateTime.now(Clock.systemUTC()).plusMinutes(2)));
        existingService.getProperties().put(OidcFederationDefaultTrustChainResolver.TEMPORARY_OPENIDFEDERATION_SERVICE,
            new DefaultRegisteredServiceProperty(Boolean.TRUE.toString()));
        servicesManager.save(existingService);

        val registeredService = resolveByClientId(clientId);
        assertNotNull(registeredService);
        assertEquals("new-service", registeredService.getName());
    }

    @Test
    void verifyMissingServiceWithNonFederatedEntityIdIsIgnored() throws Throwable {
        clearOAuthRegisteredServices();
        val clientId = "rp-non-federated-" + UUID.randomUUID();
        val registeredService = resolveByClientId(clientId);
        assertNull(registeredService);
        assertFalse(servicesManager.getAllServicesOfType(OAuthRegisteredService.class)
            .stream()
            .anyMatch(service -> service.getClientId().equals(clientId)));
    }

    @Test
    void verifyTemporaryServiceWithFarExpirationIsNotRefreshed() throws Throwable {
        clearOAuthRegisteredServices();
        val clientId = "https://rp-refresh-" + UUID.randomUUID() + ".example.org";
        val existingService = buildOidcRegisteredService(clientId, "old-service");
        existingService.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(true,
            ZonedDateTime.now(Clock.systemUTC()).plusMinutes(30)));
        existingService.getProperties().put(OidcFederationDefaultTrustChainResolver.TEMPORARY_OPENIDFEDERATION_SERVICE,
            new DefaultRegisteredServiceProperty(Boolean.TRUE.toString()));
        servicesManager.save(existingService);
        val registeredService = resolveByClientId(clientId);
        assertNotNull(registeredService);
        assertEquals("old-service", registeredService.getName());
    }

    @Test
    void verifyServiceWithoutTemporaryFlagIsNotRefreshed() throws Throwable {
        clearOAuthRegisteredServices();
        val clientId = "https://rp-refresh-" + UUID.randomUUID() + ".example.org";
        val existingService = buildOidcRegisteredService(clientId, "old-service");
        existingService.setExpirationPolicy(new DefaultRegisteredServiceExpirationPolicy(true,
            ZonedDateTime.now(Clock.systemUTC()).plusMinutes(2)));
        servicesManager.save(existingService);
        val registeredService = resolveByClientId(clientId);
        assertNotNull(registeredService);
        assertEquals("old-service", registeredService.getName());
    }

    private void clearOAuthRegisteredServices() {
        val services = new ArrayList<>(servicesManager.getAllServicesOfType(OAuthRegisteredService.class));
        services.forEach(servicesManager::delete);
    }

    private OAuthRegisteredService resolveByClientId(final String clientId) throws Throwable {
        val validator = (OpenIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidator) authorizationRequestValidator;
        return validator.getRegisteredServiceByClientId(clientId);
    }

    private static OidcRegisteredService buildOidcRegisteredService(final String clientId, final String name) {
        val service = new OidcRegisteredService();
        service.setName(name);
        service.setClientId(clientId);
        service.setClientSecret(UUID.randomUUID().toString());
        service.setServiceId("https://example.org/.*");
        service.assignIdIfNecessary();
        return service;
    }

    @TestConfiguration(value = "OpenIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidatorTestConfiguration", proxyBeanMethods = false)
    static class OpenIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidatorTestConfiguration {
        @Bean(name = OidcFederationTrustChainResolver.BEAN_NAME)
        public OidcFederationTrustChainResolver oidcFederationTrustChainResolver() {
            return new TestOidcFederationTrustChainResolver();
        }
    }

    static class TestOidcFederationTrustChainResolver implements OidcFederationTrustChainResolver {

        @Override
        public Optional<OidcRegisteredService> resolveTrustChains(final String clientId) {
            if (clientId.contains("rp-missing-")) {
                return Optional.of(buildOidcRegisteredService(clientId, "resolved-service"));
            }
            if (clientId.contains("rp-refresh-")) {
                return Optional.of(buildOidcRegisteredService(clientId, "new-service"));
            }
            return Optional.empty();
        }
    }
}
