package org.apereo.cas.support.sms;

import org.apereo.cas.config.NexmoSmsConfiguration;
import org.apereo.cas.util.io.SmsSender;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link NexmoSmsSenderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    NexmoSmsConfiguration.class
}, properties = {
    "cas.sms-provider.nexmo.api-token=123456",
    "cas.sms-provider.nexmo.api-secret=123456",
    "cas.sms-provider.nexmo.signature-secret=123456"
})
@Tag("Simple")
public class NexmoSmsSenderTests {
    @Autowired
    @Qualifier("smsSender")
    private SmsSender smsSender;

    @Test
    public void verifyOperation() {
        assertTrue(smsSender.canSend());
        assertFalse(smsSender.send("3477464532", "3477462341", "This is a text"));
    }
}
