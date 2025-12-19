package org.apereo.cas.token;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.token.cipher.RegisteredServiceJwtTicketCipherExecutor;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JwtTicketBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("Tickets")
class JwtTicketBuilderTests extends BaseJwtTokenTicketBuilderTests {

    @Test
    void verifyJwtForServiceTicket() throws Throwable {
        var jwt = tokenTicketBuilder.build("ST-123455", CoreAuthenticationTestUtils.getWebApplicationService());
        assertNotNull(jwt);
        val result = tokenCipherExecutor.decode(jwt);
        val claims = JWTClaimsSet.parse(result.toString());
        assertEquals("casuser", claims.getSubject());
    }

    @Test
    void verifyJwtForServiceTicketWithOwnKeys() throws Throwable {
        val service = CoreAuthenticationTestUtils.getWebApplicationService("https://jwt.example.org/cas");
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
    void verifyJwtForTicketGrantingTicket() throws Throwable {
        val tgt = new MockTicketGrantingTicket("casuser");
        val jwt = tokenTicketBuilder.build(tgt, Map.of());
        assertNotNull(jwt);
        val result = tokenCipherExecutor.decode(jwt);
        val claims = JWTClaimsSet.parse(result.toString());
        assertEquals(claims.getSubject(), tgt.getAuthentication().getPrincipal().getId());
    }

    @Test
    void verifyJwtForAuthN() throws Throwable {
        val tgt = new MockTicketGrantingTicket("casuser");
        val jwt = tokenTicketBuilder.build(tgt.getAuthentication());
        assertNotNull(jwt);
        val result = tokenCipherExecutor.decode(jwt);
        val claims = JWTClaimsSet.parse(result.toString());
        assertEquals(claims.getSubject(), tgt.getAuthentication().getPrincipal().getId());
    }

    @Test
    void verifyJwtForAuthAndService() throws Throwable {
        val tgt = new MockTicketGrantingTicket("casuser");
        val jwt = tokenTicketBuilder.build(tgt.getAuthentication(), RegisteredServiceTestUtils.getRegisteredService());
        assertNotNull(jwt);
        val result = tokenCipherExecutor.decode(jwt);
        val claims = JWTClaimsSet.parse(result.toString());
        assertEquals(claims.getSubject(), tgt.getAuthentication().getPrincipal().getId());
    }
}
