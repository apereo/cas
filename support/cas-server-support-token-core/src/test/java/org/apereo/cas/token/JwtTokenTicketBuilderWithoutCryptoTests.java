package org.apereo.cas.token;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

import com.nimbusds.jwt.JWTParser;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JwtTokenTicketBuilderWithoutCryptoTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@TestPropertySource(properties = {
    "cas.authn.token.crypto.encryptionEnabled=false",
    "cas.authn.token.crypto.signingEnabled=false",
    "spring.mail.host=localhost",
    "spring.mail.port=25000"
})
public class JwtTokenTicketBuilderWithoutCryptoTests extends BaseJwtTokenTicketBuilderTests {

    @Test
    public void verifyJwtForServiceTicketEncoding() throws Exception {
        val jwt = tokenTicketBuilder.build("ST-123456", CoreAuthenticationTestUtils.getService());
        assertNotNull(jwt);
        val claims = JWTParser.parse(jwt).getJWTClaimsSet();
        assertEquals("casuser", claims.getSubject());
    }
}
