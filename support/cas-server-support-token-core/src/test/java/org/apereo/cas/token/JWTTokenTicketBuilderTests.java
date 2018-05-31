package org.apereo.cas.token;

import com.nimbusds.jwt.JWTClaimsSet;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.token.cipher.RegisteredServiceTokenTicketCipherExecutor;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * This is {@link JWTTokenTicketBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class JWTTokenTicketBuilderTests extends BaseJWTTokenTicketBuilderTests {

    @Test
    public void verifyJwtForServiceTicket() throws Exception {
        final var jwt = tokenTicketBuilder.build("ST-123455", CoreAuthenticationTestUtils.getService());
        assertNotNull(jwt);
        final var result = tokenCipherExecutor.decode(jwt);
        final var claims = JWTClaimsSet.parse(result.toString());
        assertEquals("casuser", claims.getSubject());
    }

    @Test
    public void verifyJwtForServiceTicketWithOwnKeys() throws Exception {
        final var service = CoreAuthenticationTestUtils.getService("https://jwt.example.org/cas");
        final var jwt = tokenTicketBuilder.build("ST-123455", service);
        assertNotNull(jwt);
        final var result = tokenCipherExecutor.decode(jwt);
        assertNull(result);

        final var registeredService = servicesManager.findServiceBy(service);
        final var cipher = new RegisteredServiceTokenTicketCipherExecutor();
        assertTrue(cipher.supports(registeredService));
        final var decoded = cipher.decode(jwt, Optional.of(registeredService));
        final var claims = JWTClaimsSet.parse(decoded);
        assertEquals("casuser", claims.getSubject());
    }

    @Test
    public void verifyJwtForTicketGrantingTicket() throws Exception {
        final var tgt = new MockTicketGrantingTicket("casuser");
        final var jwt = tokenTicketBuilder.build(tgt);
        assertNotNull(jwt);
        final var result = tokenCipherExecutor.decode(jwt);
        final var claims = JWTClaimsSet.parse(result.toString());
        assertEquals(claims.getSubject(), tgt.getAuthentication().getPrincipal().getId());
    }
}
