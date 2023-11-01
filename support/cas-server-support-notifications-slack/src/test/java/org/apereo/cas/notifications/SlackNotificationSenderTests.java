package org.apereo.cas.notifications;

import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.SlackMessagingConfiguration;
import org.apereo.cas.notifications.push.NotificationSender;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SlackNotificationSenderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    SlackMessagingConfiguration.class,
    CasCoreUtilConfiguration.class
}, properties = "cas.slack-messaging.api-token=xoxb-1234567890")
@Tag("Simple")
class SlackNotificationSenderTests {

    @Autowired
    @Qualifier("slackNotificationSender")
    private NotificationSender notificationSender;

    @Test
    void verifyOperation() throws Throwable {
        assertFalse(notificationSender.notify(RegisteredServiceTestUtils.getPrincipal("cas"),
            Map.of(NotificationSender.ATTRIBUTE_NOTIFICATION_TITLE, "Apereo CAS Notification",
                NotificationSender.ATTRIBUTE_NOTIFICATION_MESSAGE, "Hello, this message is from _Apereo CAS_")));
    }

}
