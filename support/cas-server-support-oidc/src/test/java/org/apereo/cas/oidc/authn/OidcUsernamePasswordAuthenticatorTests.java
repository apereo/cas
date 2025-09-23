package org.apereo.cas.oidc.authn;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.List;
import java.util.UUID;
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
    private Authenticator authenticator;

    @Test
    void verifyClientIdWithoutAnyAttributes() {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);

        val credentials = new UsernamePasswordCredentials("oidctest", "oidctest");
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        request.addParameter(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecret());
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials);
        assertNotNull(credentials.getUserProfile());
        assertEquals(1, credentials.getUserProfile().getAttributes().size());
        assertTrue(credentials.getUserProfile().getAttributes().containsKey(OAuth20Constants.CLIENT_ID));
    }

    @Test
    void verifyClientIdWithScopesRequest() {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
        val credentials = new UsernamePasswordCredentials("oidctest", "oidctest");
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        request.addParameter(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecret());
        request.addParameter(OAuth20Constants.SCOPE, "openid profile email");
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials);
        val userProfile = credentials.getUserProfile();
        assertNotNull(userProfile);
        assertTrue(userProfile.getAttributes().containsKey("email"));
        assertTrue(userProfile.getAttributes().containsKey("family_name"));
        assertTrue(userProfile.getAttributes().containsKey("given_name"));
    }

    @Test
    void verifyClientIdWithoutScopesRequest() {
        val registeredService = getOidcRegisteredService(UUID.randomUUID().toString());
        servicesManager.save(registeredService);
        val credentials = new UsernamePasswordCredentials("oidctest", "oidctest");
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        request.addParameter(OAuth20Constants.CLIENT_SECRET, registeredService.getClientSecret());
        request.addParameter(OAuth20Constants.SCOPE, OidcConstants.StandardScopes.OPENID.getScope());
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(new CallContext(ctx, new JEESessionStore()), credentials);
        assertNotNull(credentials.getUserProfile());
        assertEquals(1, credentials.getUserProfile().getAttributes().size());
        assertTrue(credentials.getUserProfile().getAttributes().containsKey(OAuth20Constants.CLIENT_ID));
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
