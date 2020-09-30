package org.apereo.cas.support.oauth.validator.token.device;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InvalidOAuth20DeviceTokenExceptionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
public class InvalidOAuth20DeviceTokenExceptionTests {
    @Test
    public void verifyOperation() {
        assertThrows(InvalidOAuth20DeviceTokenException.class, new Executable() {
            @Override
            public void execute() {
                throw new InvalidOAuth20DeviceTokenException("bad-ticket-id");
            }
        });
    }
}
