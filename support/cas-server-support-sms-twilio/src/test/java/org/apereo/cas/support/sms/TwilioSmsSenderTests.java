package org.apereo.cas.support.sms;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TwilioSmsSenderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class TwilioSmsSenderTests {
    @Test
    public void verifyAction() {
        val s = new TwilioSmsSender("accountid", "token");
        assertFalse(s.send("123456789", "123456789", "Msg"));
    }
}
