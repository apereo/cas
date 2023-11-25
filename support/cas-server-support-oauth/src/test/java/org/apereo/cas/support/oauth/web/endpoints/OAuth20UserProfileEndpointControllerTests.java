package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenCipherExecutor;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20RegisteredServiceJwtAccessTokenCipherExecutor;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenFactory;
import org.apereo.cas.ticket.accesstoken.OAuth20DefaultAccessTokenFactory;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This class tests the {@link OAuth20UserProfileEndpointController} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
@Tag("OAuthWeb")
@TestPropertySource(properties = "cas.ticket.track-descendant-tickets=false")
class OAuth20UserProfileEndpointControllerTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("oauth20ProtocolEndpointConfigurer")
    private CasWebSecurityConfigurer<Void> oauth20ProtocolEndpointConfigurer;

    @Autowired
    @Qualifier("defaultAccessTokenFactory")
    private OAuth20AccessTokenFactory accessTokenFactory;
    @Autowired
    @Qualifier("oauthProfileController")
    private OAuth20UserProfileEndpointController oAuth20ProfileController;

    protected static Authentication getAuthentication(final Principal principal) {
        val metadata = new BasicIdentifiableCredential(principal.getId());
        val handlerResult = new DefaultAuthenticationHandlerExecutionResult(principal.getClass().getCanonicalName(),
            metadata, principal, new ArrayList<>());

        return DefaultAuthenticationBuilder.newInstance()
            .setPrincipal(principal)
            .addCredential(metadata)
            .setAuthenticationDate(ZonedDateTime.now(ZoneId.systemDefault()))
            .addSuccess(principal.getClass().getCanonicalName(), handlerResult)
            .build();
    }

    @Test
    void verifyNoGivenAccessToken() throws Throwable {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(),
            CONTEXT + OAuth20Constants.PROFILE_URL);
        val mockResponse = new MockHttpServletResponse();

        val entity = oAuth20ProfileController.handlePostRequest(mockRequest, mockResponse);

        assertEquals(HttpStatus.UNAUTHORIZED, entity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, mockResponse.getContentType());
        assertNotNull(entity.getBody());
        assertTrue(entity.getBody().toString().contains(OAuth20Constants.MISSING_ACCESS_TOKEN));
    }

    @Test
    void verifyNoExistingAccessToken() throws Throwable {
        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.PROFILE_URL);
        mockRequest.setParameter(OAuth20Constants.ACCESS_TOKEN, "DOES NOT EXIST");
        val mockResponse = new MockHttpServletResponse();

        val entity = oAuth20ProfileController.handleGetRequest(mockRequest, mockResponse);

        assertEquals(HttpStatus.UNAUTHORIZED, entity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, mockResponse.getContentType());
        assertNotNull(entity.getBody());
        assertTrue(entity.getBody().toString().contains(OAuth20Constants.EXPIRED_ACCESS_TOKEN));
    }

    @Test
    void verifyExpiredAccessToken() throws Throwable {
        val principal = CoreAuthenticationTestUtils.getPrincipal(ID, new HashMap<>());
        val authentication = getAuthentication(principal);
        val jwtBuilder = new JwtBuilder(new OAuth20JwtAccessTokenCipherExecutor(), servicesManager,
            new OAuth20RegisteredServiceJwtAccessTokenCipherExecutor(), casProperties);
        val expiringAccessTokenFactory = new OAuth20DefaultAccessTokenFactory(
            alwaysExpiresExpirationPolicyBuilder(), jwtBuilder, servicesManager, descendantTicketsTrackingPolicy);

        val code = addCode(principal, addRegisteredService());
        val accessToken = expiringAccessTokenFactory.create(RegisteredServiceTestUtils.getService(), authentication,
            new MockTicketGrantingTicket("casuser"), new ArrayList<>(), code.getId(), code.getClientId(), new HashMap<>(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        this.ticketRegistry.addTicket(accessToken);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.PROFILE_URL);
        mockRequest.setParameter(OAuth20Constants.ACCESS_TOKEN, accessToken.getId());
        val mockResponse = new MockHttpServletResponse();

        val entity = oAuth20ProfileController.handleGetRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.UNAUTHORIZED, entity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, mockResponse.getContentType());
        assertNotNull(entity.getBody());
        assertTrue(entity.getBody().toString().contains(OAuth20Constants.EXPIRED_ACCESS_TOKEN));
    }

    @Test
    void verifyEndpoints() throws Throwable {
        assertFalse(oauth20ProtocolEndpointConfigurer.getIgnoredEndpoints().isEmpty());
    }

    @Test
    void verifyOK() throws Throwable {
        val map = new HashMap<String, List<Object>>();
        map.put(NAME, List.of(VALUE));
        val list = List.of(VALUE, VALUE);
        map.put(NAME2, (List) list);

        val principal = CoreAuthenticationTestUtils.getPrincipal(ID, map);
        val authentication = getAuthentication(principal);
        val code = addCode(principal, addRegisteredService());

        val accessToken = accessTokenFactory.create(RegisteredServiceTestUtils.getService(), authentication,
            new MockTicketGrantingTicket("casuser"), new ArrayList<>(), code.getId(), code.getClientId(), new HashMap<>(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        this.ticketRegistry.addTicket(accessToken);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.PROFILE_URL);
        mockRequest.setParameter(OAuth20Constants.ACCESS_TOKEN, accessToken.getId());
        val mockResponse = new MockHttpServletResponse();

        val entity = oAuth20ProfileController.handleGetRequest(mockRequest, mockResponse);

        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, mockResponse.getContentType());

        val receivedBody = (Map) entity.getBody();
        assertEquals(ID, receivedBody.get("id"));
        val attributes = (Map<String, List>) receivedBody.get("attributes");
        assertEquals(VALUE, attributes.get(NAME).get(0));
        assertEquals(list, attributes.get(NAME2));
    }
    
    @Test
    void verifyOKWithExpiredTicketGrantingTicket() throws Throwable {
        val map = new HashMap<String, List<Object>>();
        map.put(NAME, List.of(VALUE));
        val list = List.of(VALUE, VALUE);
        map.put(NAME2, (List) list);

        val principal = CoreAuthenticationTestUtils.getPrincipal(ID, map);
        val authentication = getAuthentication(principal);
        val code = addCode(principal, addRegisteredService());

        val accessToken = accessTokenFactory.create(RegisteredServiceTestUtils.getService(), authentication,
            new MockTicketGrantingTicket("casuser"), new ArrayList<>(),
            code.getId(), code.getClientId(), new HashMap<>(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        accessToken.getTicketGrantingTicket().markTicketExpired();
        this.ticketRegistry.addTicket(accessToken);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.PROFILE_URL);
        mockRequest.setParameter(OAuth20Constants.ACCESS_TOKEN, accessToken.getId());
        val mockResponse = new MockHttpServletResponse();

        val entity = oAuth20ProfileController.handleGetRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, mockResponse.getContentType());

        val expectedObj = MAPPER.createObjectNode();
        val attrNode = MAPPER.createObjectNode();
        attrNode.put(NAME, VALUE);
        val values = MAPPER.createArrayNode();
        values.add(VALUE);
        values.add(VALUE);
        attrNode.put(NAME2, values);
        expectedObj.put("id", ID);
        expectedObj.put("attributes", attrNode);

        val receivedBody = (Map) entity.getBody();
        assertEquals(ID, receivedBody.get("id"));
        val attributes = (Map<String, List>) receivedBody.get("attributes");
        assertEquals(VALUE, attributes.get(NAME).get(0));
        assertEquals(list, attributes.get(NAME2));
    }

    @Test
    void verifyOKWithAuthorizationHeader() throws Throwable {
        val map = new HashMap<String, List<Object>>();
        map.put(NAME, List.of(VALUE));
        val list = List.of(VALUE, VALUE);
        map.put(NAME2, (List) list);

        val principal = CoreAuthenticationTestUtils.getPrincipal(ID, map);
        val authentication = getAuthentication(principal);
        val code = addCode(principal, addRegisteredService());
        val accessToken = accessTokenFactory.create(RegisteredServiceTestUtils.getService(), authentication,
            new MockTicketGrantingTicket("casuser"), new ArrayList<>(),
            code.getId(), code.getClientId(), new HashMap<>(),
            OAuth20ResponseTypes.CODE, OAuth20GrantTypes.AUTHORIZATION_CODE);
        this.ticketRegistry.addTicket(accessToken);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.PROFILE_URL);
        mockRequest.addHeader("Authorization", OAuth20Constants.TOKEN_TYPE_BEARER + ' ' + accessToken.getId());
        val mockResponse = new MockHttpServletResponse();
        val entity = oAuth20ProfileController.handleGetRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, mockResponse.getContentType());

        val receivedBody = (Map) entity.getBody();
        assertEquals(ID, receivedBody.get("id"));
        val attributes = (Map<String, List>) receivedBody.get("attributes");
        assertEquals(VALUE, attributes.get(NAME).get(0));
        assertEquals(list, attributes.get(NAME2));
    }
}
