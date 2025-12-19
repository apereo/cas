package org.apereo.cas.oidc.web.response;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcJwtResponseModeCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("OIDC")
class OidcJwtResponseModeCipherExecutorTests extends AbstractOidcTests {

    @TestPropertySource(properties = {
        "cas.authn.oidc.response.crypto.signing-enabled=false",
        "cas.authn.oidc.response.crypto.encryption-enabled=true",
        "cas.authn.oidc.response.crypto.strategy-type=SIGN_AND_ENCRYPT"
    })
    @Nested
    class EncryptOnlyTests extends AbstractOidcTests {
        @Test
        void verifyOperation() throws Throwable {
            val at = getAccessToken();
            val encoded = oidcResponseModeJwtCipherExecutor.encode(at.getId());
            assertNotNull(encoded);
            val decoded = oidcResponseModeJwtCipherExecutor.decode(encoded);
            assertNotNull(decoded);
            assertEquals(at.getId(), decoded);
        }
    }

    @TestPropertySource(properties = {
        "cas.authn.oidc.response.crypto.signing-enabled=true",
        "cas.authn.oidc.response.crypto.encryption-enabled=false",
        "cas.authn.oidc.response.crypto.strategy-type=SIGN_AND_ENCRYPT"
    })
    @Nested
    class SignOnlyTests extends AbstractOidcTests {
        @Test
        void verifyOperation() throws Throwable {
            val at = getAccessToken();
            val encoded = oidcResponseModeJwtCipherExecutor.encode(at.getId());
            assertNotNull(encoded);
            val decoded = oidcResponseModeJwtCipherExecutor.decode(encoded);
            assertNotNull(decoded);
            assertEquals(at.getId(), decoded);
        }
    }

    @TestPropertySource(properties = {
        "cas.authn.oidc.response.crypto.signing-enabled=true",
        "cas.authn.oidc.response.crypto.encryption-enabled=true",
        "cas.authn.oidc.response.crypto.strategy-type=SIGN_AND_ENCRYPT"
    })
    @Nested
    class SignAndEncryptTests extends AbstractOidcTests {
        @Test
        void verifyOperation() throws Throwable {
            val at = getAccessToken();
            val encoded = oidcResponseModeJwtCipherExecutor.encode(at.getId());
            assertNotNull(encoded);
            val decoded = oidcResponseModeJwtCipherExecutor.decode(encoded);
            assertNotNull(decoded);
            assertEquals(at.getId(), decoded);
        }
    }
}
