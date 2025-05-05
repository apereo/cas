package org.apereo.cas.tokens;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.token.TokenConstants;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JwtTicketGrantingTicketResourceEntityResponseFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("Tickets")
@TestPropertySource(properties = {
    "cas.authn.attribute-repository.stub.attributes.uid=casuser",
    "cas.authn.attribute-repository.stub.attributes.givenName=apereo-cas",
    "cas.authn.attribute-repository.stub.attributes.phone=123456789"
})
class JwtTicketGrantingTicketResourceEntityResponseFactoryTests extends BaseTicketResourceEntityResponseFactoryTests {
    
    @BeforeEach
    void before() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("223.456.789.000");
        request.setLocalAddr("223.456.789.100");
        request.addHeader(HttpHeaders.USER_AGENT, "Firefox");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
    }

    @Test
    void verifyTicketGrantingTicketAsDefault() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(casProperties.getServer().getPrefix());
        servicesManager.save(registeredService);

        val result = CoreAuthenticationTestUtils.getAuthenticationResult(authenticationSystemSupport);
        val tgt = centralAuthenticationService.createTicketGrantingTicket(result);

        val response = ticketGrantingTicketResourceEntityResponseFactory.build(tgt, new MockHttpServletRequest());
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void verifyTicketGrantingTicketAsJwt() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(casProperties.getServer().getPrefix());
        servicesManager.save(registeredService);

        val result = CoreAuthenticationTestUtils.getAuthenticationResult(authenticationSystemSupport,
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));
        val tgt = (TicketGrantingTicket) centralAuthenticationService.createTicketGrantingTicket(result);

        val request = new MockHttpServletRequest();
        request.addParameter(TokenConstants.PARAMETER_NAME_TOKEN, Boolean.TRUE.toString());
        request.addParameter("customParameter", "customParameterValue1");
        request.addParameter("customParameter", "customParameterValue2");
        val response = ticketGrantingTicketResourceEntityResponseFactory.build(tgt, request);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        val jwt = tokenCipherExecutor.decode(response.getBody());
        val claims = JWTClaimsSet.parse(jwt.toString());
        assertEquals(claims.getSubject(), tgt.getAuthentication().getPrincipal().getId());
        assertEquals(2, claims.getStringArrayClaim("customParameter").length);
        assertNull(claims.getStringClaim(TokenConstants.PARAMETER_NAME_TOKEN));
        assertNotNull(claims.getStringClaim(RestHttpRequestCredentialFactory.PARAMETER_USERNAME));
        assertNull(claims.getStringClaim(RestHttpRequestCredentialFactory.PARAMETER_PASSWORD));
        assertEquals(2, claims.getStringArrayClaim("customParameter").length);
    }

    @Test
    void verifyTicketGrantingTicketAsJwtWithHeader() throws Throwable {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(casProperties.getServer().getPrefix());
        servicesManager.save(registeredService);

        val result = CoreAuthenticationTestUtils.getAuthenticationResult(authenticationSystemSupport,
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));
        val tgt = (TicketGrantingTicket) centralAuthenticationService.createTicketGrantingTicket(result);

        val request = new MockHttpServletRequest();
        request.addHeader(TokenConstants.PARAMETER_NAME_TOKEN, Boolean.TRUE.toString());
        val response = ticketGrantingTicketResourceEntityResponseFactory.build(tgt, request);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        val jwt = tokenCipherExecutor.decode(response.getBody());
        val claims = JWTClaimsSet.parse(jwt.toString());
        assertEquals(claims.getSubject(), tgt.getAuthentication().getPrincipal().getId());
    }

}
