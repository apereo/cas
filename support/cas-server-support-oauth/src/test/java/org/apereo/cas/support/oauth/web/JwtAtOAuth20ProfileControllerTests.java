package org.apereo.cas.support.oauth.web;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20UserProfileEndpointController;
import org.apereo.cas.ticket.CatalogUsingMapTicketRegistry;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.DateTimeUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This class tests the {@link OAuth20UserProfileEndpointController} class with .
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
@ContextConfiguration(classes = {JwtAtOAuth20ProfileControllerTests.TestConfig.class})
@DirtiesContext
@Tag("OAuth")
public class JwtAtOAuth20ProfileControllerTests extends OAuth20ProfileControllerTests {

    @Autowired
    @Qualifier("defaultAccessTokenFactory")
    private AccessTokenFactory accessTokenFactory;

    @Autowired
    @Qualifier("profileController")
    private OAuth20UserProfileEndpointController oAuth20ProfileController;


    @Autowired
    @Qualifier("accessTokenJwtBuilder")
    private JwtBuilder accessTokenJwtBuilder;

    @Test
    public void verifyOKJwtAccessToken() throws Exception {
        val map = new HashMap<String, List<Object>>();
        map.put(NAME, List.of(VALUE));
        val list = List.of(VALUE, VALUE);
        map.put(NAME2, (List) list);

        val principal = CoreAuthenticationTestUtils.getPrincipal(ID, map);
        val authentication = getAuthentication(principal);
        val service = addRegisteredService();
        service.setJwtAccessToken(true);
        val accessToken = accessTokenFactory.create(RegisteredServiceTestUtils.getService(), authentication,
                new MockTicketGrantingTicket("casuser"), new ArrayList<>(), service.getClientId(), new HashMap<>());
        this.ticketRegistry.addTicket(accessToken);

        val mockRequest = new MockHttpServletRequest(HttpMethod.GET.name(), CONTEXT + OAuth20Constants.PROFILE_URL);
        mockRequest.setParameter(OAuth20Constants.ACCESS_TOKEN, encodeAccessToken(accessToken));
        val mockResponse = new MockHttpServletResponse();

        val entity = oAuth20ProfileController.handleGetRequest(mockRequest, mockResponse);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, mockResponse.getContentType());

        val expected = "{\"id\":\"" + ID + "\",\"attributes\":[{\"" + NAME + "\":\"" + VALUE + "\"},{\"" + NAME2
                + "\":[\"" + VALUE + "\",\"" + VALUE + "\"]}]}";
        val expectedObj = MAPPER.readTree(expected);
        val receivedObj = MAPPER.readTree(entity.getBody());
        assertEquals(expectedObj.get("id").asText(), receivedObj.get("id").asText());

        val expectedAttributes = expectedObj.get(ATTRIBUTES_PARAM);
        val receivedAttributes = receivedObj.get(ATTRIBUTES_PARAM);

        assertEquals(expectedAttributes.findValue(NAME).asText(), receivedAttributes.findValue(NAME).asText());
        assertEquals(expectedAttributes.findValues(NAME2), receivedAttributes.findValues(NAME2));
    }

    private String encodeAccessToken(final AccessToken accessToken) {
        val dt = ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(15);
        val builder = JwtBuilder.JwtRequest.builder();

        val request = builder
                .serviceAudience(accessToken.getService().getId())
                .issueDate(DateTimeUtils.dateOf(accessToken.getCreationTime()))
                .jwtId(accessToken.getId())
                .subject(accessToken.getAuthentication().getPrincipal().getId())
                .validUntilDate(DateTimeUtils.dateOf(dt))
                .attributes(accessToken.getAuthentication().getAttributes())
                .build();
        return accessTokenJwtBuilder.build(request);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean("ticketRegistry")
        public TicketRegistry ticketRegistry(final TicketCatalog ticketCatalog) {
            return new CatalogUsingMapTicketRegistry(ticketCatalog);
        }
    }
}
