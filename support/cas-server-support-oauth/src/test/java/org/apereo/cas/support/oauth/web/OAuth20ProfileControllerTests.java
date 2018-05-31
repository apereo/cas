package org.apereo.cas.support.oauth.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.BasicCredentialMetaData;
import org.apereo.cas.authentication.BasicIdentifiableCredential;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.CredentialMetaData;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20UserProfileEndpointController;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.accesstoken.DefaultAccessTokenFactory;
import org.apereo.cas.ticket.support.AlwaysExpiresExpirationPolicy;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This class tests the {@link OAuth20UserProfileEndpointController} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */

@Slf4j
public class OAuth20ProfileControllerTests extends AbstractOAuth20Tests {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String CONTEXT = "/oauth2.0/";
    private static final String ID = "1234";
    private static final String NAME = "attributeName";
    private static final String NAME2 = "attributeName2";
    private static final String VALUE = "attributeValue";
    private static final String CONTENT_TYPE = "application/json";
    private static final String GET = "GET";
    private static final String ATTRIBUTES_PARAM = "attributes";

    @Autowired
    @Qualifier("defaultAccessTokenFactory")
    private AccessTokenFactory accessTokenFactory;

    @Autowired
    @Qualifier("profileController")
    private OAuth20UserProfileEndpointController oAuth20ProfileController;

    @Test
    public void verifyNoGivenAccessToken() throws Exception {
        final var mockRequest = new MockHttpServletRequest(GET, CONTEXT + OAuth20Constants.PROFILE_URL);
        final var mockResponse = new MockHttpServletResponse();

        final var entity = oAuth20ProfileController.handleRequest(mockRequest, mockResponse);

        assertEquals(HttpStatus.UNAUTHORIZED, entity.getStatusCode());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());
        assertTrue(entity.getBody().contains(OAuth20Constants.MISSING_ACCESS_TOKEN));
    }

    @Test
    public void verifyNoExistingAccessToken() throws Exception {
        final var mockRequest = new MockHttpServletRequest(GET, CONTEXT + OAuth20Constants.PROFILE_URL);
        mockRequest.setParameter(OAuth20Constants.ACCESS_TOKEN, "DOES NOT EXIST");
        final var mockResponse = new MockHttpServletResponse();

        final var entity = oAuth20ProfileController.handleRequest(mockRequest, mockResponse);

        assertEquals(HttpStatus.UNAUTHORIZED, entity.getStatusCode());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());
        assertTrue(entity.getBody().contains(OAuth20Constants.EXPIRED_ACCESS_TOKEN));
    }

    @Test
    public void verifyExpiredAccessToken() throws Exception {
        final var principal = CoreAuthenticationTestUtils.getPrincipal(ID, new HashMap<>());
        final var authentication = getAuthentication(principal);
        final var expiringAccessTokenFactory = new DefaultAccessTokenFactory(new AlwaysExpiresExpirationPolicy());
        final var accessToken = expiringAccessTokenFactory.create(RegisteredServiceTestUtils.getService(), authentication,
            new MockTicketGrantingTicket("casuser"), new ArrayList<>());
        this.ticketRegistry.addTicket(accessToken);

        final var mockRequest = new MockHttpServletRequest(GET, CONTEXT + OAuth20Constants.PROFILE_URL);
        mockRequest.setParameter(OAuth20Constants.ACCESS_TOKEN, accessToken.getId());
        final var mockResponse = new MockHttpServletResponse();

        final var entity = oAuth20ProfileController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.UNAUTHORIZED, entity.getStatusCode());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());
        assertTrue(entity.getBody().contains(OAuth20Constants.EXPIRED_ACCESS_TOKEN));
    }

    @Test
    public void verifyOK() throws Exception {
        final Map<String, Object> map = new HashMap<>();
        map.put(NAME, VALUE);
        final var list = Arrays.asList(VALUE, VALUE);
        map.put(NAME2, list);

        final var principal = CoreAuthenticationTestUtils.getPrincipal(ID, map);
        final var authentication = getAuthentication(principal);
        final var accessToken = accessTokenFactory.create(RegisteredServiceTestUtils.getService(), authentication,
            new MockTicketGrantingTicket("casuser"), new ArrayList<>());
        this.ticketRegistry.addTicket(accessToken);

        final var mockRequest = new MockHttpServletRequest(GET, CONTEXT + OAuth20Constants.PROFILE_URL);
        mockRequest.setParameter(OAuth20Constants.ACCESS_TOKEN, accessToken.getId());
        final var mockResponse = new MockHttpServletResponse();

        final var entity = oAuth20ProfileController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final var expected = "{\"id\":\"" + ID + "\",\"attributes\":[{\"" + NAME + "\":\"" + VALUE + "\"},{\"" + NAME2
            + "\":[\"" + VALUE + "\",\"" + VALUE + "\"]}]}";
        final var expectedObj = MAPPER.readTree(expected);
        final var receivedObj = MAPPER.readTree(entity.getBody());
        assertEquals(expectedObj.get("id").asText(), receivedObj.get("id").asText());

        final var expectedAttributes = expectedObj.get(ATTRIBUTES_PARAM);
        final var receivedAttributes = receivedObj.get(ATTRIBUTES_PARAM);

        assertEquals(expectedAttributes.findValue(NAME).asText(), receivedAttributes.findValue(NAME).asText());
        assertEquals(expectedAttributes.findValues(NAME2), receivedAttributes.findValues(NAME2));
    }

    @Test
    public void verifyOKWithExpiredTicketGrantingTicket() throws Exception {
        final Map<String, Object> map = new HashMap<>();
        map.put(NAME, VALUE);
        final var list = Arrays.asList(VALUE, VALUE);
        map.put(NAME2, list);

        final var principal = CoreAuthenticationTestUtils.getPrincipal(ID, map);
        final var authentication = getAuthentication(principal);
        final var accessToken = accessTokenFactory.create(RegisteredServiceTestUtils.getService(), authentication,
            new MockTicketGrantingTicket("casuser"), new ArrayList<>());
        accessToken.getTicketGrantingTicket().markTicketExpired();
        this.ticketRegistry.addTicket(accessToken);

        final var mockRequest = new MockHttpServletRequest(GET, CONTEXT + OAuth20Constants.PROFILE_URL);
        mockRequest.setParameter(OAuth20Constants.ACCESS_TOKEN, accessToken.getId());
        final var mockResponse = new MockHttpServletResponse();

        final var entity = oAuth20ProfileController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final var expectedObj = MAPPER.createObjectNode();
        final var attrNode = MAPPER.createObjectNode();
        attrNode.put(NAME, VALUE);
        final var values = MAPPER.createArrayNode();
        values.add(VALUE);
        values.add(VALUE);
        attrNode.put(NAME2, values);
        expectedObj.put("id", ID);
        expectedObj.put("attributes", attrNode);

        final var receivedObj = MAPPER.readTree(entity.getBody());
        assertEquals(expectedObj.get("id").asText(), receivedObj.get("id").asText());

        final var expectedAttributes = expectedObj.get(ATTRIBUTES_PARAM);
        final var receivedAttributes = receivedObj.get(ATTRIBUTES_PARAM);

        assertEquals(expectedAttributes.findValue(NAME).asText(), receivedAttributes.findValue(NAME).asText());
        assertEquals(expectedAttributes.findValues(NAME2), receivedAttributes.findValues(NAME2));
    }

    @Test
    public void verifyOKWithAuthorizationHeader() throws Exception {
        final Map<String, Object> map = new HashMap<>();
        map.put(NAME, VALUE);
        final var list = Arrays.asList(VALUE, VALUE);
        map.put(NAME2, list);

        final var principal = CoreAuthenticationTestUtils.getPrincipal(ID, map);
        final var authentication = getAuthentication(principal);
        final var accessToken = accessTokenFactory.create(RegisteredServiceTestUtils.getService(), authentication,
            new MockTicketGrantingTicket("casuser"), new ArrayList<>());
        this.ticketRegistry.addTicket(accessToken);

        final var mockRequest = new MockHttpServletRequest(GET, CONTEXT + OAuth20Constants.PROFILE_URL);
        mockRequest.addHeader("Authorization", OAuth20Constants.BEARER_TOKEN + ' ' + accessToken.getId());
        final var mockResponse = new MockHttpServletResponse();
        final var entity = oAuth20ProfileController.handleRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final var expected = "{\"id\":\"" + ID + "\",\"attributes\":[{\"" + NAME + "\":\"" + VALUE + "\"},{\"" + NAME2
            + "\":[\"" + VALUE + "\",\"" + VALUE + "\"]}]}";
        final var expectedObj = MAPPER.readTree(expected);
        final var receivedObj = MAPPER.readTree(entity.getBody());
        assertEquals(expectedObj.get("id").asText(), receivedObj.get("id").asText());

        final var expectedAttributes = expectedObj.get(ATTRIBUTES_PARAM);
        final var receivedAttributes = receivedObj.get(ATTRIBUTES_PARAM);

        assertEquals(expectedAttributes.findValue(NAME).asText(), receivedAttributes.findValue(NAME).asText());
        assertEquals(expectedAttributes.findValues(NAME2), receivedAttributes.findValues(NAME2));
    }

    protected static Authentication getAuthentication(final Principal principal) {
        final CredentialMetaData metadata = new BasicCredentialMetaData(new BasicIdentifiableCredential(principal.getId()));
        final AuthenticationHandlerExecutionResult handlerResult = new DefaultAuthenticationHandlerExecutionResult(principal.getClass().getCanonicalName(),
            metadata, principal, new ArrayList<>());

        return DefaultAuthenticationBuilder.newInstance()
            .setPrincipal(principal)
            .addCredential(metadata)
            .setAuthenticationDate(ZonedDateTime.now())
            .addSuccess(principal.getClass().getCanonicalName(), handlerResult)
            .build();
    }
}
