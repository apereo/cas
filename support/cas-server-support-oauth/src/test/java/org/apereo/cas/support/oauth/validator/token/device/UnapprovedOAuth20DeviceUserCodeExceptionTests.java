package org.apereo.cas.support.oauth.validator.token.device;

import module java.base;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UnapprovedOAuth20DeviceUserCodeExceptionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
class UnapprovedOAuth20DeviceUserCodeExceptionTests {
    @Test
    void verifyOperation() {
        assertThrows(UnapprovedOAuth20DeviceUserCodeException.class, () -> {
            throw new UnapprovedOAuth20DeviceUserCodeException("bad-ticket");
        });
    }
}
