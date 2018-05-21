package org.apereo.cas.tokens;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.token.TokenConstants;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.*;

/**
 * This is {@link JWTTicketGrantingTicketResourceEntityResponseFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class JWTTicketGrantingTicketResourceEntityResponseFactoryTests extends BaseTicketResourceEntityResponseFactoryTests {
    @Test
    public void verifyTicketGrantingTicketAsDefault() throws Exception {
        final AuthenticationResult result = CoreAuthenticationTestUtils.getAuthenticationResult(authenticationSystemSupport);
        final TicketGrantingTicket tgt = centralAuthenticationService.createTicketGrantingTicket(result);

        final ResponseEntity<String> response = ticketGrantingTicketResourceEntityResponseFactory.build(tgt, new MockHttpServletRequest());
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void verifyTicketGrantingTicketAsJwt() throws Exception {
        final AuthenticationResult result = CoreAuthenticationTestUtils.getAuthenticationResult(authenticationSystemSupport,
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));
        final TicketGrantingTicket tgt = centralAuthenticationService.createTicketGrantingTicket(result);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(TokenConstants.PARAMETER_NAME_TOKEN, Boolean.TRUE.toString());
        final ResponseEntity<String> response = ticketGrantingTicketResourceEntityResponseFactory.build(tgt, request);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        final Object jwt = this.tokenCipherExecutor.decode(response.getBody());
        final JWTClaimsSet claims = JWTClaimsSet.parse(jwt.toString());
        assertEquals(claims.getSubject(), tgt.getAuthentication().getPrincipal().getId());
    }

    @Test
    public void verifyTicketGrantingTicketAsJwtWithHeader() throws Exception {
        final AuthenticationResult result = CoreAuthenticationTestUtils.getAuthenticationResult(authenticationSystemSupport,
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));
        final TicketGrantingTicket tgt = centralAuthenticationService.createTicketGrantingTicket(result);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TokenConstants.PARAMETER_NAME_TOKEN, Boolean.TRUE.toString());
        final ResponseEntity<String> response = ticketGrantingTicketResourceEntityResponseFactory.build(tgt, request);
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        final Object jwt = this.tokenCipherExecutor.decode(response.getBody());
        final JWTClaimsSet claims = JWTClaimsSet.parse(jwt.toString());
        assertEquals(claims.getSubject(), tgt.getAuthentication().getPrincipal().getId());
    }

}
