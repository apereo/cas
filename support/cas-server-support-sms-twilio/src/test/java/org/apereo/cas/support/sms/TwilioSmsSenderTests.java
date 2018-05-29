package org.apereo.cas.support.sms;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is {@link TwilioSmsSenderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class TwilioSmsSenderTests {
    @Test
    public void verifyAction() {
        final var s = new TwilioSmsSender("accountid", "token");
        assertFalse(s.send("123456789", "123456789", "Msg"));
    }
}
