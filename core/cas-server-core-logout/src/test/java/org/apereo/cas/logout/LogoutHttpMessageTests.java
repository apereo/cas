package org.apereo.cas.logout;

import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.*;

/**
 * This is {@link LogoutHttpMessageTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class LogoutHttpMessageTests {
    @Test
    public void verifyOperation() throws Exception {
        final var message = new LogoutHttpMessage(new URL("https://github.com"), "LogoutMessage", false);
        assertTrue(message.getMessage().startsWith(LogoutHttpMessage.LOGOUT_REQUEST_PARAMETER));
    }
}
