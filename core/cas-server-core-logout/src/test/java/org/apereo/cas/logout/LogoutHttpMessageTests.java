package org.apereo.cas.logout;

import lombok.val;
import org.junit.jupiter.api.Test;

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
        val message = new LogoutHttpMessage(new URL("https://github.com"), "LogoutMessage", false);
        assertTrue(message.getMessage().startsWith(LogoutHttpMessage.LOGOUT_REQUEST_PARAMETER));
    }
}
