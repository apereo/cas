package org.apereo.cas.support.validation;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SecurityTokenServiceCredentialCipherExecutorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WSFederation")
public class SecurityTokenServiceCredentialCipherExecutorTests {

    @Test
    public void verifyOperation() {
        val cipher = new SecurityTokenServiceCredentialCipherExecutor(null,
            null, null, 0, 0);
        val encoded = cipher.encode("value");
        assertNotNull(encoded);
        assertEquals("value", cipher.decode(encoded));
    }
}
