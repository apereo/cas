package org.apereo.cas.config;

import org.apereo.cas.util.io.SmsSender;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    CasCoreHttpConfiguration.class,
    AmazonSimpleNotificationServiceSmsConfiguration.class
}, properties = {
    "cas.smsProvider.sns.endpoint=http://127.0.0.1:8811",
    "cas.smsProvider.sns.credentialAccessKey=test",
    "cas.smsProvider.sns.credentialSecretKey=test"
})
public class AmazonSimpleNotificationServiceSmsConfigurationTests {

    @Autowired
    @Qualifier("smsSender")
    private SmsSender smsSender;

    @Test
    public void verifyOperation() {
        assertNotNull(smsSender);
    }
}
