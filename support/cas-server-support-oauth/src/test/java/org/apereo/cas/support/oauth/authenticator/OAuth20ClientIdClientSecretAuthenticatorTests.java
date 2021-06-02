package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.services.RegisteredServiceAccessStrategyAuditableEnforcer;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuth20RegisteredServiceCipherExecutor;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.JEESessionStore;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This is {@link OAuth20ClientIdClientSecretAuthenticatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("OAuth")
@DirtiesContext
public class OAuth20ClientIdClientSecretAuthenticatorTests extends BaseOAuth20AuthenticatorTests {
    protected OAuth20ClientIdClientSecretAuthenticator authenticator;

    @BeforeEach
    public void init() {
        authenticator = new OAuth20ClientIdClientSecretAuthenticator(servicesManager,
            serviceFactory,
            new RegisteredServiceAccessStrategyAuditableEnforcer(),
            new OAuth20RegisteredServiceCipherExecutor(),
            ticketRegistry,
            defaultPrincipalResolver);
    }

    @RetryingTest(3)
    public void verifyAuthentication() {
        val credentials = new UsernamePasswordCredentials("client", "secret");
        val request = new MockHttpServletRequest();
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(credentials, ctx, JEESessionStore.INSTANCE);
        assertNotNull(credentials.getUserProfile());
        assertEquals("client", credentials.getUserProfile().getId());
    }

    @Test
    public void verifyAuthenticationWithGrantTypePassword() {
        val credentials = new UsernamePasswordCredentials("client", "secret");
        val request = new MockHttpServletRequest();
        val ctx = new JEEContext(request, new MockHttpServletResponse());

        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.PASSWORD.name());

        authenticator.validate(credentials, ctx, JEESessionStore.INSTANCE);
        assertNull(credentials.getUserProfile());
    }

    @Test
    public void verifyAuthenticationWithGrantTypeRefreshToken() {
        val refreshToken = getRefreshToken(serviceWithoutSecret);
        ticketRegistry.addTicket(refreshToken);
        
        val credentials = new UsernamePasswordCredentials("serviceWithoutSecret", refreshToken.getId());
        val request = new MockHttpServletRequest();
        val ctx = new JEEContext(request, new MockHttpServletResponse());

        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
        request.addParameter(OAuth20Constants.CLIENT_ID, "serviceWithoutSecret");
        request.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());

        assertFalse(authenticator.canAuthenticate(ctx));
        authenticator.validate(credentials, ctx, JEESessionStore.INSTANCE);
        assertNull(credentials.getUserProfile());


        request.removeAllParameters();
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
        request.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        assertTrue(authenticator.canAuthenticate(ctx));


        request.removeAllParameters();
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.REFRESH_TOKEN.name());
        request.addParameter(OAuth20Constants.CLIENT_ID, "serviceWithoutSecret");
        request.addParameter(OAuth20Constants.CLIENT_SECRET, "serviceWithoutSecret");
        request.addParameter(OAuth20Constants.REFRESH_TOKEN, refreshToken.getId());
        assertTrue(authenticator.canAuthenticate(ctx));
    }

    @Test
    public void verifyAuthenticationWithAttributesMapping() {
        val credentials = new UsernamePasswordCredentials("serviceWithAttributesMapping", "secret");
        val request = new MockHttpServletRequest();
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        authenticator.validate(credentials, ctx, JEESessionStore.INSTANCE);
        assertNotNull(credentials.getUserProfile());
        assertEquals("servicewithattributesmapping", credentials.getUserProfile().getId());
        assertNotNull(credentials.getUserProfile().getAttribute("eduPersonAffiliation"));
        assertNull(credentials.getUserProfile().getAttribute("groupMembership"));
    }

    @Test
    public void verifyAuthenticationWithoutResolvedPrincipal() {
        val id = "serviceWithAttributesMapping";
        val credentials = new UsernamePasswordCredentials(id, "secret");
        PrincipalResolver mockPrincipalResolver = mock(PrincipalResolver.class);
        when(mockPrincipalResolver.resolve(any())).thenReturn(new NullPrincipal());

        val nullPrincipalAuthenticator = new OAuth20ClientIdClientSecretAuthenticator(servicesManager,
                serviceFactory,
                new RegisteredServiceAccessStrategyAuditableEnforcer(),
                new OAuth20RegisteredServiceCipherExecutor(),
                ticketRegistry,
                mockPrincipalResolver);

        val request = new MockHttpServletRequest();
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        nullPrincipalAuthenticator.validate(credentials, ctx, JEESessionStore.INSTANCE);
        assertNotNull(credentials.getUserProfile());
        assertEquals(id, credentials.getUserProfile().getId());
    }
}
