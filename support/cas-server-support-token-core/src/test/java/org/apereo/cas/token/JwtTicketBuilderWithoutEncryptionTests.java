package org.apereo.cas.token;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.token.cipher.RegisteredServiceJwtTicketCipherExecutor;
import org.apereo.cas.util.EncodingUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JwtTicketBuilderWithoutEncryptionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@TestPropertySource(properties = "cas.authn.token.crypto.encryption-enabled=false")
@Tag("Tickets")
class JwtTicketBuilderWithoutEncryptionTests extends BaseJwtTokenTicketBuilderTests {

    @Test
    void verifyJwtForServiceTicket() throws Throwable {
        val jwt = tokenTicketBuilder.build("ST-123456", CoreAuthenticationTestUtils.getWebApplicationService());
        assertNotNull(jwt);
        val result = tokenCipherExecutor.decode(jwt);
        val claims = JWTClaimsSet.parse(result.toString());
        assertEquals("casuser", claims.getSubject());
    }

    @Test
    void verifyJwtForServiceTicketEncoding() throws Throwable {
        val jwt = tokenTicketBuilder.build("ST-123456", CoreAuthenticationTestUtils.getWebApplicationService());
        assertNotNull(jwt);
        val jwtDec = EncodingUtils.decodeBase64ToString(jwt);
        assertNotNull(jwtDec);
    }

    @Test
    void verifyJwtForServiceTicketWithoutEncryptionKey() throws Throwable {
        val service = CoreAuthenticationTestUtils.getWebApplicationService("https://jwt.no-encryption-key.example.org/cas");
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
