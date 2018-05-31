package org.apereo.cas.tokens;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.ServiceTicket;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.*;

/**
 * This is {@link JWTServiceTicketResourceEntityResponseFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class JWTServiceTicketResourceEntityResponseFactoryTests extends BaseTicketResourceEntityResponseFactoryTests {

    @Test
    public void verifyServiceTicketAsDefault() {
        final var result = CoreAuthenticationTestUtils.getAuthenticationResult(authenticationSystemSupport);
        final var tgt = centralAuthenticationService.createTicketGrantingTicket(result);
        final Service service = RegisteredServiceTestUtils.getService("test");
        final var response = serviceTicketResourceEntityResponseFactory.build(tgt.getId(), service, result);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void verifyServiceTicketAsJwt() throws Exception {
        final var result = CoreAuthenticationTestUtils.getAuthenticationResult(authenticationSystemSupport,
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));
        final var tgt = centralAuthenticationService.createTicketGrantingTicket(result);
        final Service service = RegisteredServiceTestUtils.getService("jwtservice");
        final var response = serviceTicketResourceEntityResponseFactory.build(tgt.getId(), service, result);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().startsWith(ServiceTicket.PREFIX));

        final var jwt = this.tokenCipherExecutor.decode(response.getBody());
        final var claims = JWTClaimsSet.parse(jwt.toString());
        assertEquals(claims.getSubject(), tgt.getAuthentication().getPrincipal().getId());
    }
}
