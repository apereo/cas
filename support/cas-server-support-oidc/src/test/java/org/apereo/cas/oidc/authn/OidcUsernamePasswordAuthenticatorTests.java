package org.apereo.cas.oidc.authn;

import module java.base;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.authenticator.OAuth20ClientCredentialsAuthenticator;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcUsernamePasswordAuthenticatorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("OIDCAuthentication")
@Import(OidcUsernamePasswordAuthenticatorTests.AuthenticationTestConfiguration.class)
class OidcUsernamePasswordAuthenticatorTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oauthUserAuthenticator")
    private OAuth20ClientCredentialsAuthenticator authenticator;

    @Test
    void verifyClientIdWithoutAnyAttributes() {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);

        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        request.addParameter(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecrets().getFirst().getValue());
        val credentialsProfile = authenticator.authenticate(newClientAuthenticationToken(request, "oidctest", "oidctest"));
        assertTrue(credentialsProfile.isPresent());
        assertEquals(1, credentialsProfile.orElseThrow().getAttributes().size());
        assertTrue(credentialsProfile.orElseThrow().getAttributes().containsKey(OAuth20Constants.CLIENT_ID));
    }

    @Test
    void verifyClientIdWithScopesRequest() {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        request.addParameter(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecrets().getFirst().getValue());
        request.addParameter(OAuth20Constants.SCOPE, "openid profile email");
        val credentialsProfile = authenticator.authenticate(newClientAuthenticationToken(request, "oidctest", "oidctest"));
        val userProfile = credentialsProfile.orElseThrow();
        assertNotNull(userProfile);
        assertTrue(userProfile.getAttributes().containsKey("email"));
        assertTrue(userProfile.getAttributes().containsKey("family_name"));
        assertTrue(userProfile.getAttributes().containsKey("given_name"));
    }

    @Test
    void verifyClientIdWithoutScopesRequest() {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        request.addParameter(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecrets().getFirst().getValue());
        request.addParameter(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope());
        val credentialsProfile = authenticator.authenticate(newClientAuthenticationToken(request, "oidctest", "oidctest"));
        assertTrue(credentialsProfile.isPresent());
        assertEquals(1, credentialsProfile.orElseThrow().getAttributes().size());
        assertTrue(credentialsProfile.orElseThrow().getAttributes().containsKey(OAuth20Constants.CLIENT_ID));
    }

    @Tag("OAuth")
    @TestConfiguration(value = "AuthenticationTestConfiguration", proxyBeanMethods = false)
    static class AuthenticationTestConfiguration implements AuthenticationEventExecutionPlanConfigurer {
        @Override
        public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
            val handler = new SimpleTestUsernamePasswordAuthenticationHandler();
            handler.putAttributes(CoreAuthenticationTestUtils.getAttributes());
            handler.putAttribute("family_name", List.of("Apereo"))
                .putAttribute("given_name", List.of("CAS"))
                .putAttribute("email", List.of("cas@apereo.org"));
            plan.registerAuthenticationHandler(handler);
        }
    }

}
