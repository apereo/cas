package org.apereo.cas.token;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.token.cipher.RegisteredServiceJwtTicketCipherExecutor;
import org.apereo.cas.util.EncodingUtils;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.text.ParseException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JwtTokenTicketBuilderWithoutEncryptionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@TestPropertySource(properties = "cas.authn.token.crypto.encryptionEnabled=false")
@Tag("Simple")
public class JwtTokenTicketBuilderWithoutEncryptionTests extends BaseJwtTokenTicketBuilderTests {

    @Test
    public void verifyJwtForServiceTicket() throws ParseException {
        val jwt = tokenTicketBuilder.build("ST-123456", CoreAuthenticationTestUtils.getService());
        assertNotNull(jwt);
        val result = tokenCipherExecutor.decode(jwt);
        val claims = JWTClaimsSet.parse(result.toString());
        assertEquals("casuser", claims.getSubject());
    }

    @Test
    public void verifyJwtForServiceTicketEncoding() {
        val jwt = tokenTicketBuilder.build("ST-123456", CoreAuthenticationTestUtils.getService());
        assertNotNull(jwt);
        val jwtDec = EncodingUtils.decodeBase64ToString(jwt);
        assertNotNull(jwtDec);
    }

    @Test
    public void verifyJwtForServiceTicketWithoutEncryptionKey() throws Exception {
        val service = CoreAuthenticationTestUtils.getService("https://jwt.no-encryption-key.example.org/cas");
        val jwt = tokenTicketBuilder.build("ST-123456", service);
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

}
