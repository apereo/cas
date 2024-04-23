package org.apereo.cas.support.sms;

import org.apereo.cas.config.CasNexmoSmsAutoConfiguration;
import org.apereo.cas.notifications.sms.SmsSender;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
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
    WebMvcAutoConfiguration.class,
    CasNexmoSmsAutoConfiguration.class
}, properties = {
    "cas.sms-provider.nexmo.api-token=123456",
    "cas.sms-provider.nexmo.api-secret=123456",
    "cas.sms-provider.nexmo.signature-secret=123456"
})
@Tag("SMS")
class NexmoSmsSenderTests {
    @Autowired
    @Qualifier(SmsSender.BEAN_NAME)
    private SmsSender smsSender;

    @Test
    void verifyOperation() throws Throwable {
        assertTrue(smsSender.canSend());
        assertFalse(smsSender.send("3477464532", "3477462341", "This is a text"));
    }
}
