package org.apereo.cas.token;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.EncodingUtils;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.*;

/**
 * This is {@link JWTTokenTicketBuilderWithoutEncryptionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@TestPropertySource(properties = "cas.authn.token.crypto.encryptionEnabled=false")
public class JWTTokenTicketBuilderWithoutEncryptionTests extends BaseJWTTokenTicketBuilderTests {

    @Test
    public void verifyJwtForServiceTicket() throws Exception {
        val jwt = tokenTicketBuilder.build("ST-123456", CoreAuthenticationTestUtils.getService());
        assertNotNull(jwt);
        val result = tokenCipherExecutor.decode(jwt);
        val claims = JWTClaimsSet.parse(result.toString());
        assertEquals("casuser", claims.getSubject());
    }

    @Test
    public void verifyJwtForServiceTicketEncoding() throws Exception {
        val jwt = tokenTicketBuilder.build("ST-123456", CoreAuthenticationTestUtils.getService());
        assertNotNull(jwt);
        val jwtDec = EncodingUtils.decodeBase64ToString(jwt);
        assertNotNull(jwtDec);
    }

}
