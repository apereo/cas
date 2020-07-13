package org.apereo.cas.config;

import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.push.NotificationSender;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderValidatorAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasCoreNotificationsConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    MailSenderAutoConfiguration.class,
    MailSenderValidatorAutoConfiguration.class
},
    properties = {
        "spring.mail.host=localhost",
        "spring.mail.port=25000"
    })
@Tag("Mail")
@EnabledIfPortOpen(port = 25000)
public class CasCoreNotificationsConfigurationTests {
    @Autowired
    @Qualifier("notificationSender")
    private NotificationSender notificationSender;

    @Autowired
    @Qualifier("smsSender")
    private SmsSender smsSender;

    @Autowired
    @Qualifier("communicationsManager")
    private CommunicationsManager communicationsManager;

    @Test
    public void verifyOperation() {
        assertNotNull(notificationSender);
        assertNotNull(smsSender);
        assertNotNull(communicationsManager);
    }

}
