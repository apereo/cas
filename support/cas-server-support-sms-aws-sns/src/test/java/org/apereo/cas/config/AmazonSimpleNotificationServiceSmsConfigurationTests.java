package org.apereo.cas.config;

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
 * This is {@link AmazonSimpleNotificationServiceSmsConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("AmazonWebServices")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasAmazonSimpleNotificationServiceSmsAutoConfiguration.class
}, properties = {
    "cas.sms-provider.sns.endpoint=http://127.0.0.1:8811",
    "cas.sms-provider.sns.region=us-east-1",
    "cas.sms-provider.sns.credential-access-key=test",
    "cas.sms-provider.sns.credential-secret-key=test"
})
class AmazonSimpleNotificationServiceSmsConfigurationTests {

    @Autowired
    @Qualifier(SmsSender.BEAN_NAME)
    private SmsSender smsSender;

    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(smsSender);
    }
}
