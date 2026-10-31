package org.apereo.cas.support.oauth.web;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20CasAuthenticationInterceptorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("OAuthToken")
class OAuth20CasAuthenticationInterceptorTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
    private CasCookieBuilder ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier("casCallbackUrlResolver")
    private OAuth20CasCallbackUrlResolver casCallbackUrlResolver;

    private OAuth20CasAuthenticationInterceptor interceptor;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    @BeforeEach
    void setup() {
        interceptor = new OAuth20CasAuthenticationInterceptor(oauthDistributedSessionStore, ticketGrantingTicketCookieGenerator,
            ticketRegistry, servicesManager, oauthRequestParameterResolver, casCallbackUrlResolver, oauthCasClientRedirectActionBuilder);
        request = new MockHttpServletRequest(HttpMethod.GET.name(), "/cas" + OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.AUTHORIZE_URL);
        request.setScheme(CAS_SCHEME);
        request.setServerName(CAS_SERVER);
        request.setServerPort(CAS_PORT);
        request.setContextPath("/cas");
        request.addHeader(HttpHeaders.USER_AGENT, "Firefox");
        response = new MockHttpServletResponse();
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
    }

    @Test
    void verifyStatelessOperation() throws Throwable {
        val profile = new DefaultOAuth20UserProfile("casuser");
        profile.addAttribute(Principal.class.getName(), RegisteredServiceTestUtils.getPrincipal("casuser"));
        profile.addAttribute(OAuth20Constants.CAS_OAUTH_STATELESS_PROPERTY, Boolean.TRUE);
        storeProfileIntoSession(request, profile);
        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertEquals(profile.getId(), getAuthenticatedProfile().getId());
    }

    @Test
    void verifyLoadWithValidTicket() throws Throwable {
        storeProfileIntoSession(request, new DefaultOAuth20UserProfile("casuser"));
        addTicketGrantingCookie(new MockTicketGrantingTicket(UUID.randomUUID().toString()));
        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertEquals("casuser", getAuthenticatedProfile().getId());
        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
    }

    @Test
    void verifyExpiredTicketRedirectsToLogin() throws Throwable {
        storeProfileIntoSession(request, new DefaultOAuth20UserProfile("casuser"));
        val ticketGrantingTicket = new MockTicketGrantingTicket(UUID.randomUUID().toString());
        addTicketGrantingCookie(ticketGrantingTicket);
        ticketGrantingTicket.markTicketExpired();
        ticketRegistry.updateTicket(ticketGrantingTicket);
        assertRedirectsToLogin();
    }

    @Test
    void verifyNoTicketRedirectsToLogin() throws Throwable {
        storeProfileIntoSession(request, new DefaultOAuth20UserProfile("casuser"));
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        request.setQueryString("client_id=client");
        val loginUrl = assertRedirectsToLogin();
        val service = EncodingUtils.urlDecode(loginUrl.substring(loginUrl.indexOf(CasProtocolConstants.PARAMETER_SERVICE + '=') + 8));
        assertEquals(casCallbackUrlResolver.compute(request), service);
        assertEquals(CAS_SCHEME + "://" + CAS_SERVER + "/cas" + OAuth20Constants.BASE_OAUTH20_URL + '/'
            + OAuth20Constants.AUTHORIZE_URL + "?client_id=client", oauthDistributedSessionStore.get(request, OAuth20Constants.SESSION_ATTRIBUTE_REQUESTED_URL).orElseThrow());
    }

    @Test
    void verifyProfileForAnotherClientIsNotUsed() throws Throwable {
        val profile = new DefaultOAuth20UserProfile("casuser");
        profile.addAttribute(OAuth20Constants.PROFILE_ATTRIBUTE_CLIENT_ID, "client1");
        storeProfileIntoSession(request, profile);
        addTicketGrantingCookie(new MockTicketGrantingTicket(UUID.randomUUID().toString()));
        request.addParameter(OAuth20Constants.CLIENT_ID, "client2");
        assertRedirectsToLogin();

        request.setParameter(OAuth20Constants.CLIENT_ID, "CLIENT1");
        assertRedirectsToLogin();

        request.setParameter(OAuth20Constants.CLIENT_ID, "client1");
        response = new MockHttpServletResponse();
        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertEquals(profile.getId(), getAuthenticatedProfile().getId());
    }

    @Test
    void verifyProfileWithoutClientIsNotUsedForClient() throws Throwable {
        storeProfileIntoSession(request, new DefaultOAuth20UserProfile("casuser"));
        addTicketGrantingCookie(new MockTicketGrantingTicket(UUID.randomUUID().toString()));
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        assertRedirectsToLogin();
    }

    @Test
    void verifyPostRequestIsSavedAsGet() throws Throwable {
        request.setMethod(HttpMethod.POST.name());
        request.addParameter(OAuth20Constants.CLIENT_ID, "client");
        request.addParameter(OAuth20Constants.REDIRECT_URI, "https://example.org/cb?x=1");
        assertRedirectsToLogin();
        assertEquals(CAS_SCHEME + "://" + CAS_SERVER + "/cas" + OAuth20Constants.BASE_OAUTH20_URL + '/'
            + OAuth20Constants.AUTHORIZE_URL + "?client_id=client&redirect_uri=https%3A%2F%2Fexample.org%2Fcb%3Fx%3D1",
            oauthDistributedSessionStore.get(request, OAuth20Constants.SESSION_ATTRIBUTE_REQUESTED_URL).orElseThrow());
    }

    @Test
    void verifyAjaxRequestIsRefused() throws Throwable {
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertNull(response.getHeader(HttpHeaders.LOCATION));
        assertEquals("Bearer realm=\"CAS\"", response.getHeader(HttpHeaders.WWW_AUTHENTICATE));
        assertTrue(oauthDistributedSessionStore.get(request, OAuth20Constants.SESSION_ATTRIBUTE_REQUESTED_URL).isEmpty());

        request.removeHeader("X-Requested-With");
        request.addHeader("is_ajax_request", "true");
        request.addParameter("is_ajax_request", "true");
        response = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(HttpStatus.FOUND.value(), response.getStatus());
        assertNotNull(response.getHeader(HttpHeaders.LOCATION));
    }

    @Test
    void verifyUnknownSessionValueIsIgnored() throws Throwable {
        oauthDistributedSessionStore.set(request, response, OAuth20Constants.SESSION_ATTRIBUTE_AUTHENTICATED_PROFILE, "casuser");
        addTicketGrantingCookie(new MockTicketGrantingTicket(UUID.randomUUID().toString()));
        assertRedirectsToLogin();
        assertNull(request.getAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE));
    }

    private OAuth20UserProfile getAuthenticatedProfile() {
        return (OAuth20UserProfile) Objects.requireNonNull(request.getAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE));
    }

    private void addTicketGrantingCookie(final MockTicketGrantingTicket ticketGrantingTicket) throws Throwable {
        ticketRegistry.addTicket(ticketGrantingTicket);
        val cookieResponse = new MockHttpServletResponse();
        val cookie = ticketGrantingTicketCookieGenerator.addCookie(request, cookieResponse, ticketGrantingTicket.getId());
        assertNotNull(cookie);
        request.setCookies(cookieResponse.getCookies());
    }

    private String assertRedirectsToLogin() throws Throwable {
        response = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(HttpStatus.FOUND.value(), response.getStatus());
        val location = response.getHeader(HttpHeaders.LOCATION);
        assertNotNull(location);
        assertTrue(location.startsWith(casProperties.getServer().getLoginUrl() + '?' + CasProtocolConstants.PARAMETER_SERVICE + '='));
        assertFalse(request.getAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE) instanceof OAuth20UserProfile);
        return location;
    }
}
