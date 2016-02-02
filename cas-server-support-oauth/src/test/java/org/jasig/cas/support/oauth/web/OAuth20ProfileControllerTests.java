package org.jasig.cas.support.oauth.web;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.support.oauth.OAuthConstants;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pac4j.http.profile.HttpProfile;
import org.pac4j.jwt.JwtConstants;
import org.pac4j.jwt.profile.JwtGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.mvc.Controller;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
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
@ContextConfiguration("classpath:/oauth-context.xml")
@DirtiesContext()
public final class OAuth20ProfileControllerTests {

    private static final String CONTEXT = "/oauth2.0/";

    private static final String ID = "1234";

    private static final String TGT_ID = "TGT-1";

    private static final String NAME = "attributeName";

    private static final String NAME2 = "attributeName2";

    private static final String VALUE = "attributeValue";

    private static final String CONTENT_TYPE = "application/json";

    @Autowired
    private Controller oauth20WrapperController;

    @Autowired
    @Qualifier("accessTokenJwtGenerator")
    private JwtGenerator accessTokenJwtGenerator;

    @Test
    public void verifyNoAccessToken() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.PROFILE_URL);
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(200, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());
        assertEquals("{\"error\":\"" + OAuthConstants.MISSING_ACCESS_TOKEN + "\"}", mockResponse.getContentAsString());
    }

    @Test
    public void verifyNoTicketGrantingTicketImpl() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.PROFILE_URL);
        mockRequest.setParameter(OAuthConstants.ACCESS_TOKEN, "DOES NOT EXIST TGT");
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(200, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());
        assertTrue(mockResponse.getContentAsString().contains(OAuthConstants.INVALID_REQUEST));
    }

    @Test
    public void verifyExpiredTicketGrantingTicketImpl() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.PROFILE_URL);
        mockRequest.setParameter(OAuthConstants.ACCESS_TOKEN, "BAD TGT ID");
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(200, mockResponse.getStatus());
        assertEquals(CONTENT_TYPE, mockResponse.getContentType());
        assertTrue(mockResponse.getContentAsString().contains(OAuthConstants.INVALID_REQUEST));
    }
    
    @Test
    public void verifyOK() throws Exception {
        final Map<String, Object> map = new HashMap<>();
        map.put(NAME, VALUE);
        final List<String> list = Arrays.asList(VALUE, VALUE);
        map.put(NAME2, list);

        final Principal p = org.jasig.cas.authentication.TestUtils.getPrincipal(ID, map);
        final TicketGrantingTicket impl = new TicketGrantingTicketImpl(TGT_ID,
                org.jasig.cas.authentication.TestUtils.getAuthentication(p), new NeverExpiresExpirationPolicy());
        final HttpProfile profile = new HttpProfile();
        profile.setId(ID);
        profile.addAttributes(map);
        profile.addAttribute(JwtConstants.EXPIRATION_TIME, Date.from(ZonedDateTime.now().plusSeconds(5).toInstant()));
        ((OAuth20WrapperController) oauth20WrapperController).getTicketRegistry().addTicket(impl);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.PROFILE_URL);
        mockRequest.setParameter(OAuthConstants.ACCESS_TOKEN, accessTokenJwtGenerator.generate(profile));
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);
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

        final Principal p = org.jasig.cas.authentication.TestUtils.getPrincipal(ID, map);
        final TicketGrantingTicket impl = new TicketGrantingTicketImpl(TGT_ID,
                org.jasig.cas.authentication.TestUtils.getAuthentication(p), new NeverExpiresExpirationPolicy());
        final HttpProfile profile = new HttpProfile();
        profile.setId(ID);
        profile.addAttributes(map);
        profile.addAttribute(JwtConstants.EXPIRATION_TIME, Date.from(ZonedDateTime.now().plusSeconds(5).toInstant()));
        ((OAuth20WrapperController) oauth20WrapperController).getTicketRegistry().addTicket(impl);

        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT
                + OAuthConstants.PROFILE_URL);
        mockRequest.addHeader("Authorization", OAuthConstants.BEARER_TOKEN + ' '
                + accessTokenJwtGenerator.generate(profile));
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);
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
