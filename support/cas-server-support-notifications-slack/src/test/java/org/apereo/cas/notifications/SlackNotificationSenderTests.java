package org.apereo.cas.notifications;

import module java.base;
import org.apereo.cas.config.CasSlackMessagingAutoConfiguration;
import org.apereo.cas.notifications.push.NotificationSender;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SlackNotificationSenderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = {
    CasSlackMessagingAutoConfiguration.class,
    BaseNotificationTests.SharedTestConfiguration.class
}, properties = "cas.slack-messaging.api-token=xoxb-1234567890")
@Tag("Simple")
@ExtendWith(CasTestExtension.class)
class SlackNotificationSenderTests {

    @Autowired
    @Qualifier("slackNotificationSender")
    private NotificationSender notificationSender;

    @Test
    void verifyOperation() {
        assertFalse(notificationSender.notify(RegisteredServiceTestUtils.getPrincipal("cas"),
            Map.of(NotificationSender.ATTRIBUTE_NOTIFICATION_TITLE, "Apereo CAS Notification",
                NotificationSender.ATTRIBUTE_NOTIFICATION_MESSAGE, "Hello, this message is from _Apereo CAS_")));
    }

}
