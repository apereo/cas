package org.apereo.cas.token;

import com.nimbusds.jwt.JWTClaimsSet;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.junit.Test;

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
        final String jwt = tokenTicketBuilder.build("ST-123455", CoreAuthenticationTestUtils.getService());
        assertNotNull(jwt);
        final Object result = tokenCipherExecutor.decode(jwt);
        final JWTClaimsSet claims = JWTClaimsSet.parse(result.toString());
        assertEquals("casuser", claims.getSubject());
    }

    @Test
    public void verifyJwtForTicketGrantingTicket() throws Exception {
        final MockTicketGrantingTicket tgt = new MockTicketGrantingTicket("casuser");
        final String jwt = tokenTicketBuilder.build(tgt);
        assertNotNull(jwt);
        final Object result = tokenCipherExecutor.decode(jwt);
        final JWTClaimsSet claims = JWTClaimsSet.parse(result.toString());
        assertEquals(claims.getSubject(), tgt.getAuthentication().getPrincipal().getId());
    }
}
