package org.apereo.cas.token;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.token.cipher.RegisteredServiceJwtTicketCipherExecutor;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JwtTokenTicketBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("Simple")
public class JwtTokenTicketBuilderTests extends BaseJwtTokenTicketBuilderTests {

    @Test
    public void verifyJwtForServiceTicket() throws Exception {
        val jwt = tokenTicketBuilder.build("ST-123455", CoreAuthenticationTestUtils.getService());
        assertNotNull(jwt);
        val result = tokenCipherExecutor.decode(jwt);
        val claims = JWTClaimsSet.parse(result.toString());
        assertEquals("casuser", claims.getSubject());
    }

    @Test
    public void verifyJwtForServiceTicketWithOwnKeys() throws Exception {
        val service = CoreAuthenticationTestUtils.getService("https://jwt.example.org/cas");
        val jwt = tokenTicketBuilder.build("ST-123455", service);
        assertNotNull(jwt);
        val result = tokenCipherExecutor.decode(jwt);
        assertNull(result);

        val registeredService = servicesManager.findServiceBy(service);
        val cipher = new RegisteredServiceJwtTicketCipherExecutor();
        assertTrue(cipher.supports(registeredService));
        val decoded = cipher.decode(jwt, Optional.of(registeredService));
        val claims = JWTClaimsSet.parse(decoded);
        assertEquals("casuser", claims.getSubject());
    }

    @Test
    public void verifyJwtForTicketGrantingTicket() throws Exception {
        val tgt = new MockTicketGrantingTicket("casuser");
        val jwt = tokenTicketBuilder.build(tgt);
        assertNotNull(jwt);
        val result = tokenCipherExecutor.decode(jwt);
        val claims = JWTClaimsSet.parse(result.toString());
        assertEquals(claims.getSubject(), tgt.getAuthentication().getPrincipal().getId());
    }
}
