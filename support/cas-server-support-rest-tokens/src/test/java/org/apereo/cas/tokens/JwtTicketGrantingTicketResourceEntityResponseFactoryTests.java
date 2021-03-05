package org.apereo.cas.tokens;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.token.TokenConstants;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JwtTicketGrantingTicketResourceEntityResponseFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("Tickets")
public class JwtTicketGrantingTicketResourceEntityResponseFactoryTests extends BaseTicketResourceEntityResponseFactoryTests {

    @Test
    public void verifyTicketGrantingTicketAsDefault() throws Exception {
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService(CoreAuthenticationTestUtils.getService().getId());
        servicesManager.save(registeredService);

        val result = CoreAuthenticationTestUtils.getAuthenticationResult(authenticationSystemSupport);
        val tgt = centralAuthenticationService.createTicketGrantingTicket(result);

        val response = ticketGrantingTicketResourceEntityResponseFactory.build(tgt, new MockHttpServletRequest());
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void verifyTicketGrantingTicketAsJwt() throws Exception {
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService(CoreAuthenticationTestUtils.getService().getId());
        servicesManager.save(registeredService);

        val result = CoreAuthenticationTestUtils.getAuthenticationResult(authenticationSystemSupport,
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));
        val tgt = centralAuthenticationService.createTicketGrantingTicket(result);

        val request = new MockHttpServletRequest();
        request.addParameter(TokenConstants.PARAMETER_NAME_TOKEN, Boolean.TRUE.toString());
        request.addParameter("customParameter", "customParameterValue1");
        request.addParameter("customParameter", "customParameterValue2");
        val response = ticketGrantingTicketResourceEntityResponseFactory.build(tgt, request);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        val jwt = this.tokenCipherExecutor.decode(response.getBody());
        val claims = JWTClaimsSet.parse(jwt.toString());
        assertEquals(claims.getSubject(), tgt.getAuthentication().getPrincipal().getId());
        assertEquals(2, claims.getStringArrayClaim("customParameter").length);
        assertNull(claims.getStringClaim(TokenConstants.PARAMETER_NAME_TOKEN));
        assertNull(claims.getStringClaim(RestHttpRequestCredentialFactory.PARAMETER_USERNAME));
        assertNull(claims.getStringClaim(RestHttpRequestCredentialFactory.PARAMETER_PASSWORD));
        assertEquals(2, claims.getStringArrayClaim("customParameter").length);
    }

    @Test
    public void verifyTicketGrantingTicketAsJwtWithHeader() throws Exception {
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService(CoreAuthenticationTestUtils.getService().getId());
        servicesManager.save(registeredService);
        val result = CoreAuthenticationTestUtils.getAuthenticationResult(authenticationSystemSupport,
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));
        val tgt = centralAuthenticationService.createTicketGrantingTicket(result);

        val request = new MockHttpServletRequest();
        request.addHeader(TokenConstants.PARAMETER_NAME_TOKEN, Boolean.TRUE.toString());
        val response = ticketGrantingTicketResourceEntityResponseFactory.build(tgt, request);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        val jwt = this.tokenCipherExecutor.decode(response.getBody());
        val claims = JWTClaimsSet.parse(jwt.toString());
        assertEquals(claims.getSubject(), tgt.getAuthentication().getPrincipal().getId());
    }

}
