package org.apereo.cas.oidc.ticket;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.response.OAuth20AuthorizationRequest;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationResponseBuilder;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcPushedAuthorizationRequestUriResponseBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("OIDC")
@TestPropertySource(properties = {
    "cas.tgc.crypto.enabled=false",
    "cas.authn.oidc.par.max-time-to-live-in-seconds=5",
    "cas.authn.oidc.par.number-of-uses=1"
})
public class OidcPushedAuthorizationRequestUriResponseBuilderTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcPushedAuthorizationRequestResponseBuilder")
    private OAuth20AuthorizationResponseBuilder oidcPushedAuthorizationRequestResponseBuilder;

    @Test
    public void verifyOperation() throws Exception {
        assertEquals(0, oidcPushedAuthorizationRequestResponseBuilder.getOrder());

        val registeredService = getOidcRegisteredService();
        val profile = new CommonProfile();
        profile.setId("casuser");

        val holder = AccessTokenRequestContext.builder()
            .clientId(registeredService.getClientId())
            .service(RegisteredServiceTestUtils.getService())
            .authentication(RegisteredServiceTestUtils.getAuthentication())
            .registeredService(registeredService)
            .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE)
            .responseType(OAuth20ResponseTypes.CODE)
            .userProfile(profile)
            .build();

        var authzRequest = OAuth20AuthorizationRequest.builder()
            .clientId(registeredService.getClientId())
            .url(OidcConstants.PUSHED_AUTHORIZE_URL)
            .grantType(OAuth20GrantTypes.AUTHORIZATION_CODE.getType())
            .responseType(OAuth20ResponseTypes.CODE.getType())
            .build();

        assertTrue(oidcPushedAuthorizationRequestResponseBuilder.supports(authzRequest));
        val mv = oidcPushedAuthorizationRequestResponseBuilder.build(holder);
        assertTrue(mv.getModel().containsKey(OidcConstants.EXPIRES_IN));
        val uri = mv.getModel().get(OidcConstants.REQUEST_URI).toString();
        var ticket = ticketRegistry.getTicket(uri, OidcPushedAuthorizationRequest.class);
        assertNotNull(ticket);

        val request = new MockHttpServletRequest();
        request.setRequestURI('/' + OidcConstants.PUSHED_AUTHORIZE_URL);
        val response = new MockHttpServletResponse();
        var context = new JEEContext(request, response);

        authzRequest = oidcPushedAuthorizationRequestResponseBuilder.toAuthorizationRequest(context,
            holder.getAuthentication(), holder.getService(), holder.getRegisteredService()).get().build();
        assertNotNull(authzRequest);
        assertFalse(authzRequest.isSingleSignOnSessionRequired());

        request.addParameter(OidcConstants.REQUEST_URI, uri);

        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);

        val c = ticketGrantingTicketCookieGenerator.addCookie(request, response, tgt.getId());
        request.setCookies(c);

        context = new JEEContext(request, response);
        authzRequest = oidcPushedAuthorizationRequestResponseBuilder.toAuthorizationRequest(context,
            holder.getAuthentication(), holder.getService(), holder.getRegisteredService()).get().build();
        val accessTokenRequest = authzRequest.getAccessTokenRequest();
        assertTrue(authzRequest.isSingleSignOnSessionRequired());
        assertNotNull(accessTokenRequest.getAuthentication());
        assertNotNull(accessTokenRequest.getRegisteredService());
        assertNotNull(accessTokenRequest.getResponseType());
        assertNotNull(accessTokenRequest.getGrantType());
        assertNotNull(accessTokenRequest.getTicketGrantingTicket());
        ticket = ticketRegistry.getTicket(uri, OidcPushedAuthorizationRequest.class);
        assertNull(ticket);
    }
}
