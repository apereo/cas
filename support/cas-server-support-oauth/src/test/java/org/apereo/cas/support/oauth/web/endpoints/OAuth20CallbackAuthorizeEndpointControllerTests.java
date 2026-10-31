package org.apereo.cas.support.oauth.web.endpoints;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import jakarta.servlet.http.Cookie;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20CallbackAuthorizeEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OAuthWeb")
class OAuth20CallbackAuthorizeEndpointControllerTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier(TicketTrackingPolicy.BEAN_NAME_SERVICE_TICKET_TRACKING)
    private TicketTrackingPolicy serviceTicketSessionTrackingPolicy;

    private OAuthRegisteredService registeredService;

    @BeforeEach
    void initialize() {
        registeredService = addRegisteredService();
    }

    @Test
    void verifyOperation() throws Throwable {
        val request = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.CALLBACK_AUTHORIZE_URL);
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val result = performOAuthRequest(request);
        assertEquals(302, result.getResponse().getStatus());
        assertEquals(REDIRECT_URI, result.getResponse().getRedirectedUrl());
    }

    @Test
    void verifyOperationClientsWithSameRedirectUri() throws Throwable {
        addRegisteredService();
        val newRegisteredService = addRegisteredService();
        addRegisteredService();

        val request = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.CALLBACK_AUTHORIZE_URL);
        request.addParameter(OAuth20Constants.CLIENT_ID, newRegisteredService.getClientId());
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val result = performOAuthRequest(request);
        assertEquals(302, result.getResponse().getStatus());
        assertEquals(REDIRECT_URI, result.getResponse().getRedirectedUrl());
    }

    @Test
    void verifyOperationWithoutRedirectUri() throws Throwable {
        val request = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.CALLBACK_AUTHORIZE_URL);
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        val result = performOAuthRequest(request);
        assertEquals(302, result.getResponse().getStatus());
        assertEquals(OAuth20CallbackAuthorizeEndpointController.DEFAULT_URL, result.getResponse().getRedirectedUrl());
    }

    @Test
    void verifyOperationWithoutClientId() throws Throwable {
        val request = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.CALLBACK_AUTHORIZE_URL);
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val result = performOAuthRequest(request);
        assertEquals(302, result.getResponse().getStatus());
        assertEquals(OAuth20CallbackAuthorizeEndpointController.DEFAULT_URL, result.getResponse().getRedirectedUrl());
    }

    @Test
    void verifyOperationBadClientId() throws Throwable {
        val request = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.CALLBACK_AUTHORIZE_URL);
        request.addParameter(OAuth20Constants.CLIENT_ID, "badClientId");
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        val result = performOAuthRequest(request);
        assertEquals(302, result.getResponse().getStatus());
        assertEquals(OAuth20CallbackAuthorizeEndpointController.DEFAULT_URL, result.getResponse().getRedirectedUrl());
    }

    @Test
    void verifyOperationBadRedirectUri() throws Throwable {
        val request = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.CALLBACK_AUTHORIZE_URL);
        request.addParameter(OAuth20Constants.CLIENT_ID, registeredService.getClientId());
        request.addParameter(OAuth20Constants.REDIRECT_URI, "http://badredirecturi");
        val result = performOAuthRequest(request);
        assertEquals(302, result.getResponse().getStatus());
        assertEquals(OAuth20CallbackAuthorizeEndpointController.DEFAULT_URL, result.getResponse().getRedirectedUrl());
    }

    @Test
    void verifyTicketResumesRequestedUrl() throws Throwable {
        val query = "client_id=%s&client_name=%s".formatted(registeredService.getClientId(), Authenticators.CAS_OAUTH_CLIENT);
        val serviceTicket = grantServiceTicketForCallback(query);
        val requestedUrl = casProperties.getServer().getPrefix() + OAuth20Constants.BASE_OAUTH20_URL + '/'
            + OAuth20Constants.AUTHORIZE_URL + "?client_id=" + registeredService.getClientId();
        val seedRequest = new MockHttpServletRequest();
        seedRequest.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        ClientInfoHolder.setClientInfo(ClientInfo.from(seedRequest));
        val seedResponse = new MockHttpServletResponse();
        oauthDistributedSessionStore.set(seedRequest, seedResponse, OAuth20Constants.SESSION_ATTRIBUTE_REQUESTED_URL, requestedUrl);

        val result = performCallbackRequest(query + "&ticket=" + serviceTicket, seedResponse.getCookies());
        assertEquals(HttpStatus.FOUND.value(), result.getResponse().getStatus());
        assertEquals(requestedUrl, result.getResponse().getRedirectedUrl());

        val request = result.getRequest();
        assertTrue(oauthDistributedSessionStore.get(request, OAuth20Constants.SESSION_ATTRIBUTE_REQUESTED_URL).isEmpty());
        val profile = (OAuth20UserProfile) oauthDistributedSessionStore.get(request, OAuth20Constants.SESSION_ATTRIBUTE_AUTHENTICATED_PROFILE).orElseThrow();
        assertEquals("casuser", profile.getId());
        assertEquals(registeredService.getClientId(), profile.getAttribute(OAuth20Constants.PROFILE_ATTRIBUTE_CLIENT_ID));
        assertEquals(OAuth20UserProfile.BASIC_PROFILE_HANDLER_NAME, profile.getAuthenticationHandlerName());
        assertEquals(Boolean.FALSE, request.getAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_NEW_LOGIN));
    }

    @Test
    void verifyTicketForAnotherServiceIsRefused() throws Throwable {
        val serviceTicket = grantServiceTicketForCallback("client_id=" + registeredService.getClientId());
        val result = performCallbackRequest("client_id=%s&client_name=%s&ticket=%s".formatted(
            registeredService.getClientId(), Authenticators.CAS_OAUTH_CLIENT, serviceTicket));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
        assertTrue(oauthDistributedSessionStore.get(result.getRequest(), OAuth20Constants.SESSION_ATTRIBUTE_AUTHENTICATED_PROFILE).isEmpty());
    }

    @Test
    void verifyUnknownTicketIsRefused() throws Throwable {
        val result = performCallbackRequest("client_name=%s&ticket=ST-%s".formatted(Authenticators.CAS_OAUTH_CLIENT, UUID.randomUUID()));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
        assertNotNull(result.getModelAndView());
        assertTrue(oauthDistributedSessionStore.get(result.getRequest(), OAuth20Constants.SESSION_ATTRIBUTE_AUTHENTICATED_PROFILE).isEmpty());
    }

    private String grantServiceTicketForCallback(final String query) throws Throwable {
        val service = OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix()) + '?' + query;
        val ticketGrantingTicket = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(ticketGrantingTicket);
        val serviceTicket = ticketGrantingTicket.grantServiceTicket(serviceFactory.createService(service), serviceTicketSessionTrackingPolicy);
        ticketRegistry.addTicket(serviceTicket);
        ticketRegistry.updateTicket(ticketGrantingTicket);
        return serviceTicket.getId();
    }

    private MvcResult performCallbackRequest(final String query, final Cookie... cookies) throws Exception {
        val uri = URI.create("/cas" + OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.CALLBACK_AUTHORIZE_URL + '?' + query);
        val builder = MockMvcRequestBuilders.get(uri).contextPath("/cas").header(HttpHeaders.USER_AGENT, "MSIE");
        if (cookies.length > 0) {
            builder.cookie(cookies);
        }
        return mockMvc.perform(builder).andReturn();
    }
}
