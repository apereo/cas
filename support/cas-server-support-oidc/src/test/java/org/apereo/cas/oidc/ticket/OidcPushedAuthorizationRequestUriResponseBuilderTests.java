package org.apereo.cas.oidc.ticket;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.response.OAuth20AuthorizationRequest;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationResponseBuilder;
import org.apereo.cas.ticket.InvalidTicketException;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

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
class OidcPushedAuthorizationRequestUriResponseBuilderTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcPushedAuthorizationRequestResponseBuilder")
    private OAuth20AuthorizationResponseBuilder oidcPushedAuthorizationRequestResponseBuilder;

    @Test
    void verifyOperation() throws Throwable {
        assertEquals(0, oidcPushedAuthorizationRequestResponseBuilder.getOrder());

        val registeredService = getOidcRegisteredService();
        val profile = new CommonProfile();
        profile.setId("casuser");

        val holder = AccessTokenRequestContext.builder()
            .clientId(registeredService.getClientId())
            .service(RegisteredServiceTestUtils.getService())
            .authentication(RegisteredServiceTestUtils.getAuthentication("casuser",
                Map.of("customAttribute", List.of("CASUSER-ORIGINAL"))))
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
        assertTrue(mv.getModel().containsKey(OAuth20Constants.EXPIRES_IN));
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

        val authn = RegisteredServiceTestUtils.getAuthentication("casuser",
            Map.of("customAttribute", List.of("CASUSER-TGT")));
        val tgt = new MockTicketGrantingTicket(authn);
        ticketRegistry.addTicket(tgt);

        val cookie = ticketGrantingTicketCookieGenerator.addCookie(request, response, tgt.getId());
        request.setCookies(cookie);

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
        val customAttribute = accessTokenRequest.getAuthentication().getAttributes().get("customAttribute");
        assertEquals(List.of("CASUSER-TGT", "CASUSER-ORIGINAL"), customAttribute);
        assertThrows(InvalidTicketException.class, () -> ticketRegistry.getTicket(uri, OidcPushedAuthorizationRequest.class));
    }
}
