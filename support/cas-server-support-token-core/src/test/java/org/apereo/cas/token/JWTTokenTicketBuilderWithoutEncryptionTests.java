package org.apereo.cas.token;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.val;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.token.cipher.RegisteredServiceTokenTicketCipherExecutor;
import org.apereo.cas.util.EncodingUtils;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

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
        final String jwt = tokenTicketBuilder.build("ST-123456", CoreAuthenticationTestUtils.getService());
        assertNotNull(jwt);
        final Object result = tokenCipherExecutor.decode(jwt);
        final JWTClaimsSet claims = JWTClaimsSet.parse(result.toString());
        assertEquals("casuser", claims.getSubject());
    }

    @Test
    public void verifyJwtForServiceTicketWithoutEncryptionKey() throws Exception {

        final Service service = CoreAuthenticationTestUtils.getService("https://jwt.no-encryption-key.example.org/cas");

        final String jwt = tokenTicketBuilder.build("ST-123456", service);
        assertNotNull(jwt);
        final Object result = tokenCipherExecutor.decode(jwt);
        assertNull(result);

        final RegisteredService registeredService = servicesManager.findServiceBy(service);
        final RegisteredServiceTokenTicketCipherExecutor cipher = new RegisteredServiceTokenTicketCipherExecutor();
        assertTrue(cipher.supports(registeredService));
        val decoded = cipher.decode(jwt, Optional.of(registeredService));
        val claims = JWTClaimsSet.parse(decoded);
        assertEquals("casuser", claims.getSubject());
    }

    @Test
    public void verifyJwtForServiceTicketEncoding() throws Exception {
        final String jwt = tokenTicketBuilder.build("ST-123456", CoreAuthenticationTestUtils.getService());
        assertNotNull(jwt);
        final String jwtDec = EncodingUtils.decodeBase64ToString(jwt);
        assertNotNull(jwtDec);
    }

}
