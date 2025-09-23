package org.apereo.cas.logout;

import org.apereo.cas.CasProtocolConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link LogoutHttpMessageTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("Logout")
class LogoutHttpMessageTests {
    @Test
    void verifyOperation() throws Throwable {
        val message = new LogoutHttpMessage(CasProtocolConstants.PARAMETER_LOGOUT_REQUEST, new URI("https://github.com").toURL(), "LogoutMessage", false);
        assertTrue(message.getMessage().startsWith(CasProtocolConstants.PARAMETER_LOGOUT_REQUEST));
    }
}
