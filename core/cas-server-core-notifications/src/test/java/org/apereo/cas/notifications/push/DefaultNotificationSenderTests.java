package org.apereo.cas.notifications.push;

import org.apereo.cas.config.CasCoreNotificationsConfiguration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Lazy;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultNotificationSenderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    DefaultNotificationSenderTests.DefaultNotificationSenderTestConfiguration.class,
    CasCoreNotificationsConfiguration.class
})
@Tag("Simple")
public class DefaultNotificationSenderTests {
    @Autowired
    @Qualifier("notificationSender")
    private NotificationSender notificationSender;

    @Test
    public void verifyOperation() {
        assertTrue(notificationSender.canSend());
    }

    @TestConfiguration
    @Lazy(false)
    public static class DefaultNotificationSenderTestConfiguration implements NotificationSenderExecutionPlanConfigurer {
        @Override
        public NotificationSender configureNotificationSender() {
            return NotificationSender.noOp();
        }
    }

}
