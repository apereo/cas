package org.apereo.cas.support.oauth.validator.token.device;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UnapprovedOAuth20DeviceUserCodeExceptionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
public class UnapprovedOAuth20DeviceUserCodeExceptionTests {
    @Test
    public void verifyOperation() {
        assertThrows(UnapprovedOAuth20DeviceUserCodeException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                throw new UnapprovedOAuth20DeviceUserCodeException("bad-ticket");
            }
        });
    }
}
