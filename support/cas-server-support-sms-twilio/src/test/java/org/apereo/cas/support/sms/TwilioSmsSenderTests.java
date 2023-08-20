package org.apereo.cas.support.sms;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TwilioSmsSenderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("SMS")
class TwilioSmsSenderTests {
    @Test
    void verifyAction() throws Throwable {
        val s = new TwilioSmsSender("accountid", "token");
        assertFalse(s.send("123456789", "123456789", "Msg"));
    }
}
