package org.apereo.cas.config;

import org.apereo.cas.notifications.sms.SmsSender;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TwilioSmsConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreHttpConfiguration.class,
    TwilioSmsConfiguration.class
}, properties = {
   "cas.sms-provider.twilio.account-id=id",
   "cas.sms-provider.twilio.token=token"
})
@Tag("SMS")
public class TwilioSmsConfigurationTests {
    @Autowired
    @Qualifier("smsSender")
    private SmsSender smsSender;

    @Test
    public void verifyOperation() {
        assertNotNull(smsSender);
    }
}
