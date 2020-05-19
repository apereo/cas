package org.apereo.cas.services;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.services.OidcServiceRegistryListener;
import org.apereo.cas.util.CollectionUtils;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Handles tests for {@link JpaServiceRegistry} for OIDC services.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@DirtiesContext
@Tag("JDBC")
@Import(OidcJpaServiceRegistryTests.OidcJpaServiceRegistryTestConfiguration.class)
public class OidcJpaServiceRegistryTests extends JpaServiceRegistryTests {

    @Test
    public void verifyConsentPolicyWithScopesSavedAfterLoad() {
        var svc = new OidcRegisteredService();
        svc.setName("Scopes");
        svc.setServiceId("testId");
        svc.setJwks("file:/tmp/thekeystorehere.jwks");
        svc.setClientId("client");
        svc.setClientSecret("secret");
        svc.setScopes(CollectionUtils.wrapSet(
            OidcConstants.StandardScopes.PROFILE.getScope(),
            OidcConstants.StandardScopes.ADDRESS.getScope(),
            OidcConstants.StandardScopes.OPENID.getScope()));

        this.serviceRegistry.save(svc);
        this.serviceRegistry.load();
        svc = this.serviceRegistry.findServiceByExactServiceName(svc.getName(), OidcRegisteredService.class);

        var consentPolicy = svc.getAttributeReleasePolicy().getConsentPolicy();
        assertEquals(1, consentPolicy.size());

        this.serviceRegistry.load();
        svc = this.serviceRegistry.findServiceById(svc.getId(), OidcRegisteredService.class);

        consentPolicy = svc.getAttributeReleasePolicy().getConsentPolicy();
        assertEquals(1, consentPolicy.size());
    }

    @TestConfiguration("OidcJpaServiceRegistryTestConfiguration")
    @Lazy(false)
    public static class OidcJpaServiceRegistryTestConfiguration {
        @Bean
        public ServiceRegistryListener oidcServiceRegistryListener() {
            return new OidcServiceRegistryListener(new ArrayList<>());
        }
    }
}
