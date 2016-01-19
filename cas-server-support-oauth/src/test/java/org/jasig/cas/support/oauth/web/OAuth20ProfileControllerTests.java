package org.jasig.cas.support.oauth.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.TestUtils;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.support.oauth.authentication.OAuthAuthentication;
import org.jasig.cas.ticket.AccessTokenImpl;
import org.jasig.cas.support.oauth.ticket.accesstoken.DefaultAccessTokenFactory;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketState;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This class tests the {@link OAuth20ProfileController} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:/oauth-context.xml", "classpath:/META-INF/spring/cas-servlet-oauth.xml"})
@DirtiesContext()
public final class OAuth20ProfileControllerTests {

    private static final String CONTEXT = "/oauth2.0/";

    private static final String ID = "1234";

    private static final String NAME = "attributeName";

    private static final String NAME2 = "attributeName2";

    private static final String VALUE = "attributeValue";

    private static final String CONTENT_TYPE = "application/json";

    @Autowired
    private DefaultAccessTokenFactory accessTokenFactory;

    @Autowired
    private OAuth20ProfileController oAuth20ProfileController;

    @Test
    public void verifyNoGivenAccessToken() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.PROFILE_URL);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        oAuth20ProfileController.handleRequest(mockRequest, mockResponse);
        assertEquals(200, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());
        assertEquals("{\"error\":\"" + OAuthConstants.MISSING_ACCESS_TOKEN + "\"}", mockResponse.getContentAsString());
    }

    @Test
    public void verifyNoExistingAccessToken() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.PROFILE_URL);
        mockRequest.setParameter(OAuthConstants.ACCESS_TOKEN, "DOES NOT EXIST");
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        oAuth20ProfileController.handleRequest(mockRequest, mockResponse);
        assertEquals(200, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());
        assertEquals("{\"error\":\"" + OAuthConstants.EXPIRED_ACCESS_TOKEN + "\"}", mockResponse.getContentAsString());
    }

    @Test
    public void verifyExpiredAccessToken() throws Exception {
        final Principal principal = org.jasig.cas.authentication.TestUtils.getPrincipal(ID, new HashMap<String, Object>());
        final Authentication authentication = new OAuthAuthentication(new DateTime(), principal);
        final DefaultAccessTokenFactory expiringAccessTokenFactory = new DefaultAccessTokenFactory();
        expiringAccessTokenFactory.setExpirationPolicy(new ExpirationPolicy() {
            @Override
            public boolean isExpired(final TicketState ticketState) {
                return true;
            }
        });
        final AccessTokenImpl accessToken = (AccessTokenImpl) expiringAccessTokenFactory.create(TestUtils.getService(), authentication);
        oAuth20ProfileController.getTicketRegistry().addTicket(accessToken);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.PROFILE_URL);
        mockRequest.setParameter(OAuthConstants.ACCESS_TOKEN, accessToken.getId());
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        oAuth20ProfileController.handleRequest(mockRequest, mockResponse);
        assertEquals(200, mockResponse.getStatus());
        assertEquals("{\"error\":\"" + OAuthConstants.EXPIRED_ACCESS_TOKEN + "\"}", mockResponse.getContentAsString());
    }

    @Test
    public void verifyOK() throws Exception {
        final Map<String, Object> map = new HashMap<>();
        map.put(NAME, VALUE);
        final List<String> list = Arrays.asList(VALUE, VALUE);
        map.put(NAME2, list);

        final Principal principal = org.jasig.cas.authentication.TestUtils.getPrincipal(ID, map);
        final Authentication authentication = new OAuthAuthentication(new DateTime(), principal);
        final AccessTokenImpl accessToken = (AccessTokenImpl) accessTokenFactory.create(TestUtils.getService(), authentication);
        oAuth20ProfileController.getTicketRegistry().addTicket(accessToken);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.PROFILE_URL);
        mockRequest.setParameter(OAuthConstants.ACCESS_TOKEN, accessToken.getId());
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        oAuth20ProfileController.handleRequest(mockRequest, mockResponse);
        assertEquals(200, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final ObjectMapper mapper = new ObjectMapper();

        final String expected = "{\"id\":\"" + ID + "\",\"attributes\":[{\"" + NAME + "\":\"" + VALUE + "\"},{\"" + NAME2
                + "\":[\"" + VALUE + "\",\"" + VALUE + "\"]}]}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("id").asText(), receivedObj.get("id").asText());

        final JsonNode expectedAttributes = expectedObj.get("attributes");
        final JsonNode receivedAttributes = receivedObj.get("attributes");

        assertEquals(expectedAttributes.findValue(NAME).asText(), receivedAttributes.findValue(NAME).asText());
        assertEquals(expectedAttributes.findValues(NAME2), receivedAttributes.findValues(NAME2));
    }

    @Test
    public void verifyOKWithAuthorizationHeader() throws Exception {
        final Map<String, Object> map = new HashMap<>();
        map.put(NAME, VALUE);
        final List<String> list = Arrays.asList(VALUE, VALUE);
        map.put(NAME2, list);

        final Principal principal = org.jasig.cas.authentication.TestUtils.getPrincipal(ID, map);
        final Authentication authentication = new OAuthAuthentication(new DateTime(), principal);
        final AccessTokenImpl accessToken = (AccessTokenImpl) accessTokenFactory.create(TestUtils.getService(), authentication);
        oAuth20ProfileController.getTicketRegistry().addTicket(accessToken);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.PROFILE_URL);
        mockRequest.addHeader("Authorization", OAuthConstants.BEARER_TOKEN + ' '
                + accessToken.getId());
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        oAuth20ProfileController.handleRequest(mockRequest, mockResponse);
        assertEquals(200, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());

        final ObjectMapper mapper = new ObjectMapper();

        final String expected = "{\"id\":\"" + ID + "\",\"attributes\":[{\"" + NAME + "\":\"" + VALUE + "\"},{\"" + NAME2
                + "\":[\"" + VALUE + "\",\"" + VALUE + "\"]}]}";
        final JsonNode expectedObj = mapper.readTree(expected);
        final JsonNode receivedObj = mapper.readTree(mockResponse.getContentAsString());
        assertEquals(expectedObj.get("id").asText(), receivedObj.get("id").asText());

        final JsonNode expectedAttributes = expectedObj.get("attributes");
        final JsonNode receivedAttributes = receivedObj.get("attributes");

        assertEquals(expectedAttributes.findValue(NAME).asText(), receivedAttributes.findValue(NAME).asText());
        assertEquals(expectedAttributes.findValues(NAME2), receivedAttributes.findValues(NAME2));
    }
}
