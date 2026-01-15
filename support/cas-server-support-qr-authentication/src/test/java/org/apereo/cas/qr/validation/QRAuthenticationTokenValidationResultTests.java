package org.apereo.cas.qr.validation;

import module java.base;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link QRAuthenticationTokenValidationResultTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
class QRAuthenticationTokenValidationResultTests {
    @Test
    void verifyOperation() {
        val result = QRAuthenticationTokenValidationResult.builder()
            .authentication(RegisteredServiceTestUtils.getAuthentication())
            .build();
        assertNotNull(result.getAuthentication());
    }

}
