package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.support.oauth.OAuth20Constants;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests the {@link OAuth20RevocationEndpointController} class.
 *
 * @author Julien Huon
 * @since 6.2.0
 */
@Tag("OAuth")
public class OAuth20RevocationEndpointControllerTests extends AbstractOAuth20Tests {

    private static final String PUBLIC_CLIENT_ID = "clientWithoutSecret";

    @Autowired
    @Qualifier("oauthRevocationController")
    private OAuth20RevocationEndpointController oAuth20RevocationController;

    @BeforeEach
    public void initialize() {
        clearAllServices();
    }

    @Test
    public void verifyNoGivenToken() {
        this.servicesManager.save(getRegisteredService(REDIRECT_URI, CLIENT_SECRET, new HashSet<>()));

        val mockRequest = new MockHttpServletRequest(HttpMethod.POST.name(),
            CONTEXT + OAuth20Constants.REVOCATION_URL);
        val mockResponse = new MockHttpServletResponse();
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);

        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = oAuth20RevocationController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.BAD_REQUEST.value(), mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get("error"));
    }

    @Test
    public void verifyGivenInvalidClientId() {
        this.servicesManager.save(getRegisteredService(REDIRECT_URI, CLIENT_SECRET, new HashSet<>()));

        val mockRequest = new MockHttpServletRequest(HttpMethod.POST.name(),
            CONTEXT + OAuth20Constants.REVOCATION_URL);
        val mockResponse = new MockHttpServletResponse();
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, "InvalidClientId");
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.TOKEN, "AT-1234");

        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = oAuth20RevocationController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get("error"));
    }

    @Test
    public void verifyGivenInvalidClientSecret() {
        this.servicesManager.save(getRegisteredService(REDIRECT_URI, CLIENT_SECRET, new HashSet<>()));

        val mockRequest = new MockHttpServletRequest(HttpMethod.POST.name(),
            CONTEXT + OAuth20Constants.REVOCATION_URL);
        val mockResponse = new MockHttpServletResponse();
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, WRONG_CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.TOKEN, "AT-1234");

        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = oAuth20RevocationController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), mockResponse.getStatus());
        assertEquals(OAuth20Constants.ACCESS_DENIED, mv.getModel().get("error"));
    }

    @Test
    public void verifyGivenTokenNotInRegistry() {
        this.servicesManager.save(getRegisteredService(REDIRECT_URI, CLIENT_SECRET, new HashSet<>()));
        this.servicesManager.save(getRegisteredService(REDIRECT_URI, PUBLIC_CLIENT_ID, StringUtils.EMPTY, new HashSet<>()));

        val mockRequest = new MockHttpServletRequest(HttpMethod.POST.name(),
            CONTEXT + OAuth20Constants.REVOCATION_URL);
        val mockResponse = new MockHttpServletResponse();
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.TOKEN, "AT-1234");

        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20RevocationController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.OK.value(), mockResponse.getStatus());

        mockRequest.removeAllParameters();
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, PUBLIC_CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.TOKEN, "AT-1234");

        oAuth20RevocationController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.OK.value(), mockResponse.getStatus());
    }

    @Test
    public void verifyGivenUnsupportedToken() {
        val principal = createPrincipal();
        val service = getRegisteredService(REDIRECT_URI, CLIENT_SECRET, new HashSet<>());
        this.servicesManager.save(service);

        val code = addCode(principal, service);

        val mockRequest = new MockHttpServletRequest(HttpMethod.POST.name(),
            CONTEXT + OAuth20Constants.REVOCATION_URL);
        val mockResponse = new MockHttpServletResponse();
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.TOKEN, code.getId());

        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        val mv = oAuth20RevocationController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.BAD_REQUEST.value(), mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get("error"));
    }

    @Test
    public void verifyGivenAccessTokenInRegistry() {
        val principal = createPrincipal();
        val service = getRegisteredService(REDIRECT_URI, CLIENT_SECRET, new HashSet<>());
        val publicService = getRegisteredService(REDIRECT_URI, PUBLIC_CLIENT_ID, StringUtils.EMPTY, new HashSet<>());
        this.servicesManager.save(service, publicService);


        val accessToken = addAccessToken(principal, service);
        assertNotNull(this.ticketRegistry.getTicket(accessToken.getId()));

        val mockRequest = new MockHttpServletRequest(HttpMethod.POST.name(),
            CONTEXT + OAuth20Constants.REVOCATION_URL);
        val mockResponse = new MockHttpServletResponse();
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.TOKEN, accessToken.getId());

        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20RevocationController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.OK.value(), mockResponse.getStatus());
        assertNull(this.ticketRegistry.getTicket(accessToken.getId()));


        val accessToken2 = addAccessToken(principal, publicService);
        assertNotNull(this.ticketRegistry.getTicket(accessToken2.getId()));
        mockRequest.removeAllParameters();
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, PUBLIC_CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.TOKEN, accessToken2.getId());

        oAuth20RevocationController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.OK.value(), mockResponse.getStatus());
        assertNull(this.ticketRegistry.getTicket(accessToken2.getId()));


        val accessToken3 = addAccessToken(principal, service);
        assertNotNull(this.ticketRegistry.getTicket(accessToken3.getId()));
        mockRequest.removeAllParameters();
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, PUBLIC_CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.TOKEN, accessToken3.getId());

        val mv = oAuth20RevocationController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.BAD_REQUEST.value(), mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get("error"));
    }

    @Test
    public void verifyGivenRefreshTokenInRegistry() {
        val principal = createPrincipal();
        val service = getRegisteredService(REDIRECT_URI, CLIENT_SECRET, new HashSet<>());
        val publicService = getRegisteredService(REDIRECT_URI, PUBLIC_CLIENT_ID, StringUtils.EMPTY, new HashSet<>());
        this.servicesManager.save(service, publicService);


        val accessToken = addAccessToken(principal, service);
        val refreshToken = addRefreshToken(principal, service, accessToken);
        assertNotNull(this.ticketRegistry.getTicket(accessToken.getId()));
        assertNotNull(this.ticketRegistry.getTicket(refreshToken.getId()));

        val mockRequest = new MockHttpServletRequest(HttpMethod.POST.name(),
            CONTEXT + OAuth20Constants.REVOCATION_URL);
        val mockResponse = new MockHttpServletResponse();
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        mockRequest.setParameter(OAuth20Constants.TOKEN, refreshToken.getId());

        requiresAuthenticationInterceptor.preHandle(mockRequest, mockResponse, null);
        oAuth20RevocationController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.OK.value(), mockResponse.getStatus());
        assertNull(this.ticketRegistry.getTicket(refreshToken.getId()));
        assertNull(this.ticketRegistry.getTicket(accessToken.getId()));


        val accessToken2 = addAccessToken(principal, publicService);
        val refreshToken2 = addRefreshToken(principal, publicService, accessToken2);
        assertNotNull(this.ticketRegistry.getTicket(accessToken2.getId()));
        mockRequest.removeAllParameters();
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, PUBLIC_CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.TOKEN, refreshToken2.getId());

        oAuth20RevocationController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.OK.value(), mockResponse.getStatus());
        assertNull(this.ticketRegistry.getTicket(refreshToken2.getId()));
        assertNull(this.ticketRegistry.getTicket(accessToken2.getId()));


        val refreshToken3 = addRefreshToken(principal, service);
        assertNotNull(this.ticketRegistry.getTicket(refreshToken3.getId()));
        mockRequest.removeAllParameters();
        mockRequest.setParameter(OAuth20Constants.CLIENT_ID, PUBLIC_CLIENT_ID);
        mockRequest.setParameter(OAuth20Constants.TOKEN, refreshToken3.getId());

        val mv = oAuth20RevocationController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.BAD_REQUEST.value(), mockResponse.getStatus());
        assertEquals(OAuth20Constants.INVALID_REQUEST, mv.getModel().get("error"));
    }
}
