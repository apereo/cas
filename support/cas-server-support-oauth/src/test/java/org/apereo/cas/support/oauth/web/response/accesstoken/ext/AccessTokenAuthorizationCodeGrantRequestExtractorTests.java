package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import module java.base;
import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import org.apereo.cas.support.oauth.validator.token.OAuth20InvalidTargetException;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.OAuth20UnauthorizedScopeRequestException;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AccessTokenAuthorizationCodeGrantRequestExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
class AccessTokenAuthorizationCodeGrantRequestExtractorTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("accessTokenAuthorizationCodeGrantRequestExtractor")
    private AccessTokenGrantRequestExtractor extractor;

    @Test
    void verifyNoToken() {
        val service = getRegisteredService(REDIRECT_URI, UUID.randomUUID().toString(), CLIENT_SECRET);
        servicesManager.save(service);

        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());

        val response = new MockHttpServletResponse();
        assertEquals(OAuth20ResponseTypes.NONE, extractor.getResponseType());

        assertThrows(InvalidTicketException.class, () -> extractor.extract(request, response));
    }

    @Test
    void verifyDPoPRequest() throws Throwable {
        val service = getRegisteredService(REDIRECT_URI, UUID.randomUUID().toString(), CLIENT_SECRET);
        service.setGenerateRefreshToken(true);
        servicesManager.save(service);

        val request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.USER_AGENT, "MSIE");
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());

        val principal = RegisteredServiceTestUtils.getPrincipal();
        val code = addCode(principal, service);
        ticketRegistry.addTicket(code.getTicketGrantingTicket());
        request.addParameter(OAuth20Constants.CODE, code.getId());

        val response = new MockHttpServletResponse();

        val commonProfile = new DefaultOAuth20UserProfile(service.getClientId());
        commonProfile.addAttribute(OAuth20Constants.DPOP, "dpop-value");
        commonProfile.addAttribute(OAuth20Constants.DPOP_CONFIRMATION, "dpop-confirmation-value");
        storeProfileIntoSession(request, commonProfile);

        val result = extractor.extract(request, response);
        assertNotNull(result);
        assertNotNull(result.getDpop());
        assertNotNull(result.getDpopConfirmation());
    }

    @Test
    void verifyExtraction() throws Throwable {
        val service = getRegisteredService(REDIRECT_URI, UUID.randomUUID().toString(), CLIENT_SECRET);
        service.setGenerateRefreshToken(true);
        servicesManager.save(service);

        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());

        val principal = RegisteredServiceTestUtils.getPrincipal();
        val code = addCode(principal, service);
        ticketRegistry.addTicket(code.getTicketGrantingTicket());
        request.addParameter(OAuth20Constants.CODE, code.getId());

        val response = new MockHttpServletResponse();

        val commonProfile = new DefaultOAuth20UserProfile("testuser");
        storeProfileIntoSession(request, commonProfile);

        val result = extractor.extract(request, response);
        assertNotNull(result);
    }

    @Test
    void verifyRequestedResources() throws Throwable {
        val service = getRegisteredService(REDIRECT_URI, UUID.randomUUID().toString(), CLIENT_SECRET);
        service.setAllowedResources(Set.of("https://api.example.org/", "https://other.example.org/", "https://third.example.org/"));
        servicesManager.save(service);

        val code = addCode(RegisteredServiceTestUtils.getPrincipal(), service);
        code.setResources(List.of("https://api.example.org/", "https://other.example.org/"));
        ticketRegistry.addTicket(code.getTicketGrantingTicket());
        ticketRegistry.updateTicket(code);

        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        request.addParameter(OAuth20Constants.CODE, code.getId());
        val response = new MockHttpServletResponse();
        val commonProfile = new DefaultOAuth20UserProfile("testuser");
        storeProfileIntoSession(request, commonProfile);

        assertEquals(Set.of("https://api.example.org/", "https://other.example.org/"), extractor.extract(request, response).getResources());

        request.addParameter(OAuth20Constants.RESOURCE, "https://api.example.org/");
        assertEquals(Set.of("https://api.example.org/"), extractor.extract(request, response).getResources());

        request.setParameter(OAuth20Constants.RESOURCE, "https://third.example.org/");
        assertThrows(OAuth20InvalidTargetException.class, () -> extractor.extract(request, response));

        code.setResources(List.of());
        ticketRegistry.updateTicket(code);
        request.setParameter(OAuth20Constants.RESOURCE, "https://cas.example.org/cas/mcp");
        assertEquals(Set.of("https://cas.example.org/cas/mcp"), extractor.extract(request, response).getResources());
    }

    @Test
    void verifyStatelessExtraction() throws Throwable {
        val service = getRegisteredService(REDIRECT_URI, UUID.randomUUID().toString(), CLIENT_SECRET);
        service.setGenerateRefreshToken(true);
        servicesManager.save(service);

        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());

        val principal = RegisteredServiceTestUtils.getPrincipal();
        val code = addCode(principal, service);
        code.markTicketStateless();
        request.addParameter(OAuth20Constants.CODE, code.getId());

        val response = new MockHttpServletResponse();
        val commonProfile = new DefaultOAuth20UserProfile("testuser");
        storeProfileIntoSession(request, commonProfile);
        val result = extractor.extract(request, response);
        assertNotNull(result);
    }

    @Test
    void verifyExpiredCode() throws Throwable {
        val service = getRegisteredService(REDIRECT_URI, UUID.randomUUID().toString(), CLIENT_SECRET);
        service.setGenerateRefreshToken(true);
        servicesManager.save(service);

        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());


        val principal = RegisteredServiceTestUtils.getPrincipal();
        val code = addCode(principal, service);
        ticketRegistry.addTicket(code.getTicketGrantingTicket());
        code.markTicketExpired();
        request.addParameter(OAuth20Constants.CODE, code.getId());

        val response = new MockHttpServletResponse();
        val commonProfile = new DefaultOAuth20UserProfile("testuser");
        storeProfileIntoSession(request, commonProfile);
        assertThrows(InvalidTicketException.class, () -> extractor.extract(request, response));
    }

    @Test
    void verifyExpiredTgt() throws Throwable {
        val service = getRegisteredService(REDIRECT_URI, UUID.randomUUID().toString(), CLIENT_SECRET);
        service.setGenerateRefreshToken(true);
        servicesManager.save(service);

        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REDIRECT_URI, REDIRECT_URI);
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());

        val principal = RegisteredServiceTestUtils.getPrincipal();
        val code = addCode(principal, service);
        code.getTicketGrantingTicket().markTicketExpired();
        ticketRegistry.updateTicket(code);

        request.addParameter(OAuth20Constants.CODE, code.getId());

        val response = new MockHttpServletResponse();
        assertThrows(InvalidTicketException.class, () -> extractor.extract(request, response));
    }

    @Test
    void verifyUnknownService() throws Throwable {
        val service = getRegisteredService(REDIRECT_URI, UUID.randomUUID().toString(), CLIENT_SECRET);
        servicesManager.save(service);

        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.REDIRECT_URI, "unknown.org/abc");
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
        request.addParameter(OAuth20Constants.CLIENT_ID, "Unknown");

        val principal = RegisteredServiceTestUtils.getPrincipal();
        val code = addCode(principal, service);
        ticketRegistry.addTicket(code.getTicketGrantingTicket());
        request.addParameter(OAuth20Constants.CODE, code.getId());

        val response = new MockHttpServletResponse();
        assertThrows(UnauthorizedServiceException.class, () -> extractor.extract(request, response));
    }

    @Test
    void verifyNoClientIdOrRedirectUri() throws Throwable {
        val service = getRegisteredService(REDIRECT_URI, UUID.randomUUID().toString(), CLIENT_SECRET);
        servicesManager.save(service);

        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());

        val principal = RegisteredServiceTestUtils.getPrincipal();
        val code = addCode(principal, service);
        ticketRegistry.addTicket(code.getTicketGrantingTicket());
        request.addParameter(OAuth20Constants.CODE, code.getId());

        val response = new MockHttpServletResponse();
        assertThrows(UnauthorizedServiceException.class, () -> extractor.extract(request, response));
    }

    @Test
    void verifyScopeUnauthorized() throws Throwable {
        val service = getRegisteredService(UUID.randomUUID().toString(), UUID.randomUUID().toString(), CLIENT_SECRET);
        service.setScopes(Set.of("openid", "profile"));
        servicesManager.save(service);

        val code = addCode(RegisteredServiceTestUtils.getPrincipal(), service, service.getScopes());
        val request = new MockHttpServletRequest();
        request.addParameter(OAuth20Constants.GRANT_TYPE, OAuth20GrantTypes.AUTHORIZATION_CODE.getType());
        request.addParameter(OAuth20Constants.CLIENT_ID, service.getClientId());
        request.addParameter(OAuth20Constants.CLIENT_SECRET, CLIENT_SECRET);
        request.addParameter(OAuth20Constants.CODE, code.getId());
        request.addParameter(OAuth20Constants.SCOPE, "email");

        assertThrows(OAuth20UnauthorizedScopeRequestException.class,
            () -> extractor.extract(request, new MockHttpServletResponse()));

        request.removeParameter(OAuth20Constants.SCOPE);
        var result = extractor.extract(request, new MockHttpServletResponse());
        assertEquals(result.getScopes(), code.getScopes());
        assertEquals(result.getScopes(), service.getScopes());

        request.addParameter(OAuth20Constants.SCOPE, "openid");
        result = extractor.extract(request, new MockHttpServletResponse());
        assertEquals(1, result.getScopes().size());
        assertEquals("openid", result.getScopes().iterator().next());
    }
}
