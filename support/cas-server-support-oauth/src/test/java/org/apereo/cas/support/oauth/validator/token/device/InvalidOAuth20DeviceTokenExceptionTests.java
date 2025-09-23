package org.apereo.cas.support.oauth.validator.token.device;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InvalidOAuth20DeviceTokenExceptionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
class InvalidOAuth20DeviceTokenExceptionTests {
    @Test
    void verifyOperation() {
        assertThrows(InvalidOAuth20DeviceTokenException.class, () -> {
            throw new InvalidOAuth20DeviceTokenException("bad-ticket-id");
        });
    }
}
