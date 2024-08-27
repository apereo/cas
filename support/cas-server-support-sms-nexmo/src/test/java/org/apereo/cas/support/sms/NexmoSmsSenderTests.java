package org.apereo.cas.support.sms;

import org.apereo.cas.config.CasNexmoSmsAutoConfiguration;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link NexmoSmsSenderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasNexmoSmsAutoConfiguration.class, properties = {
    "cas.sms-provider.nexmo.api-token=123456",
    "cas.sms-provider.nexmo.api-secret=123456",
    "cas.sms-provider.nexmo.signature-secret=123456"
})
@Tag("SMS")
@ExtendWith(CasTestExtension.class)
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
