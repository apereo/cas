package org.apereo.cas.tokens;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.ServiceTicket;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.*;

/**
 * This is {@link JWTServiceTicketResourceEntityResponseFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class JWTServiceTicketResourceEntityResponseFactoryTests extends BaseTicketResourceEntityResponseFactoryTests {

    @Test
    public void verifyServiceTicketAsDefault() {
        val result = CoreAuthenticationTestUtils.getAuthenticationResult(authenticationSystemSupport);
        val tgt = centralAuthenticationService.createTicketGrantingTicket(result);
        val service = RegisteredServiceTestUtils.getService("test");
        val response = serviceTicketResourceEntityResponseFactory.build(tgt.getId(), service, result);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void verifyServiceTicketAsJwt() throws Exception {
        val result = CoreAuthenticationTestUtils.getAuthenticationResult(authenticationSystemSupport,
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));
        val tgt = centralAuthenticationService.createTicketGrantingTicket(result);
        val service = RegisteredServiceTestUtils.getService("jwtservice");
        val response = serviceTicketResourceEntityResponseFactory.build(tgt.getId(), service, result);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().startsWith(ServiceTicket.PREFIX));

        val jwt = this.tokenCipherExecutor.decode(response.getBody());
        val claims = JWTClaimsSet.parse(jwt.toString());
        assertEquals(claims.getSubject(), tgt.getAuthentication().getPrincipal().getId());
    }
}
