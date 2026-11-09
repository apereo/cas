package org.apereo.cas.token;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JwtTicketBuilderWithoutCryptoTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@TestPropertySource(properties = {
    "cas.authn.token.crypto.encryption-enabled=false",
    "cas.authn.token.crypto.signing-enabled=false"
})
@Tag("Tickets")
class JwtTicketBuilderWithoutCryptoTests extends BaseJwtTokenTicketBuilderTests {

    /**
     * Without keys to sign or encrypt it, the service ticket would be an unsigned JWT, which is never issued.
     */
    @Test
    void verifyJwtForServiceTicketEncoding() {
        val service = CoreAuthenticationTestUtils.getWebApplicationService();
        assertThrows(IllegalStateException.class, () -> tokenTicketBuilder.build("ST-123456", service));
    }
}
