package org.apereo.cas.oidc.dynareg;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.web.controllers.dynareg.NoOpOidcClientConfigurationEndpointController;
import org.apereo.cas.oidc.web.controllers.dynareg.NoOpOidcDynamicClientRegistrationEndpointController;
import org.apereo.cas.oidc.web.controllers.dynareg.NoOpOidcInitialAccessTokenController;
import org.apereo.cas.oidc.web.controllers.dynareg.OidcClientConfigurationEndpointController;
import org.apereo.cas.oidc.web.controllers.dynareg.OidcDynamicClientRegistrationEndpointController;
import org.apereo.cas.oidc.web.controllers.dynareg.OidcInitialAccessTokenController;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcDynamicClientRegistrationToggleTests}.
 *
 * @author Jiří Prokop
 * @since 7.3.0
 */
@Tag("OIDC")
class OidcDynamicClientRegistrationToggleTests {
    
    @Nested
    class DefaultEnabled extends AbstractOidcTests {
        @Test
        void verifyRegistrationEnabledByDefault() {
            assertTrue(applicationContext.getBean("oidcDynamicClientRegistrationEndpointController")
             instanceof OidcDynamicClientRegistrationEndpointController);
            assertTrue(applicationContext.getBean("oidcClientConfigurationEndpointController")
                instanceof OidcClientConfigurationEndpointController);
            assertTrue(applicationContext.getBean("oidcInitialAccessTokenController")
                instanceof OidcInitialAccessTokenController);
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.registration.dynamic-client-registration-enabled=true")
    class ExplicitEnabled extends AbstractOidcTests {
        @Test
        void verifyRegistrationEnabledExplicitly() {
            assertTrue(applicationContext.getBean("oidcDynamicClientRegistrationEndpointController")
             instanceof OidcDynamicClientRegistrationEndpointController);
            assertTrue(applicationContext.getBean("oidcClientConfigurationEndpointController")
                instanceof OidcClientConfigurationEndpointController);
            assertTrue(applicationContext.getBean("oidcInitialAccessTokenController")
                instanceof OidcInitialAccessTokenController);
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.registration.dynamic-client-registration-enabled=false")
    class Disabled extends AbstractOidcTests {
        @Test
        void verifyRegistrationDisabled() {
            assertTrue(applicationContext.getBean("oidcDynamicClientRegistrationEndpointController")
             instanceof NoOpOidcDynamicClientRegistrationEndpointController);
            assertTrue(applicationContext.getBean("oidcClientConfigurationEndpointController")
                instanceof NoOpOidcClientConfigurationEndpointController);
            assertTrue(applicationContext.getBean("oidcInitialAccessTokenController")
                instanceof NoOpOidcInitialAccessTokenController);
        }
    }
}
