package org.apereo.cas.config;

import org.apereo.cas.notifications.call.PhoneCallOperator;
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
 * This is {@link TwilioConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasCoreHttpConfiguration.class,
    TwilioConfiguration.class
}, properties = {
    "cas.sms-provider.twilio.phone-calls-enabled=true",
    "cas.sms-provider.twilio.account-id=${#randomString8}",
    "cas.sms-provider.twilio.token=${#randomString8}"
})
@Tag("SMS")
class TwilioConfigurationTests {
    @Autowired
    @Qualifier(SmsSender.BEAN_NAME)
    private SmsSender smsSender;

    @Autowired
    @Qualifier(PhoneCallOperator.BEAN_NAME)
    private PhoneCallOperator phoneCallOperator;

    @Test
    void verifyAction() throws Throwable {
        assertFalse(smsSender.send("123456789", "123456789", "Msg"));
        assertFalse(phoneCallOperator.call("123456789", "123456789", "Hello There!"));
    }

}
