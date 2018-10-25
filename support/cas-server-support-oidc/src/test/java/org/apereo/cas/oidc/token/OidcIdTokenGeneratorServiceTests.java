package org.apereo.cas.oidc.token;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OidcIdTokenGeneratorServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class OidcIdTokenGeneratorServiceTests extends AbstractOidcTests {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void verifyTokenGeneration() {
        val request = new MockHttpServletRequest();
        val profile = new CommonProfile();
        profile.setClientName("OIDC");
        profile.setId("casuser");
        request.setAttribute(Pac4jConstants.USER_PROFILES, profile);

        val response = new MockHttpServletResponse();

        val tgt = mock(TicketGrantingTicket.class);
        val callback = casProperties.getServer().getPrefix()
            + OAuth20Constants.BASE_OAUTH20_URL + '/'
            + OAuth20Constants.CALLBACK_AUTHORIZE_URL_DEFINITION;

        val service = new WebApplicationServiceFactory().createService(callback);
        when(tgt.getServices()).thenReturn(CollectionUtils.wrap("service", service));
        when(tgt.getAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication());

        val accessToken = mock(AccessToken.class);
        when(accessToken.getAuthentication()).thenReturn(CoreAuthenticationTestUtils.getAuthentication("casuser"));
        when(accessToken.getTicketGrantingTicket()).thenReturn(tgt);
        when(accessToken.getId()).thenReturn(getClass().getSimpleName());

        val idToken = oidcIdTokenGenerator.generate(request, response, accessToken, 30,
            OAuth20ResponseTypes.CODE, OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, "clientid"));
        assertNotNull(idToken);
    }

    @Test
    public void verifyTokenGenerationFailsWithoutProfile() {
        thrown.expect(IllegalArgumentException.class);
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val accessToken = mock(AccessToken.class);
        oidcIdTokenGenerator.generate(request, response, accessToken, 30,
            OAuth20ResponseTypes.CODE,
            OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, "clientid"));
    }
}
