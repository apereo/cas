package org.apereo.cas.notifications.push;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.notifications.sms.SmsSender;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        assertFalse(new DefaultNotificationSender(List.of()).notify(CoreAuthenticationTestUtils.getPrincipal(), Map.of()));

        val sender = mock(NotificationSender.class);
        when(sender.getOrder()).thenCallRealMethod();
        assertEquals(Ordered.LOWEST_PRECEDENCE, sender.getOrder());

        val smsSender = mock(SmsSender.class);
        when(smsSender.send(anyString(), anyString(), anyString())).thenCallRealMethod();
        assertFalse(smsSender.send("1", "2", "3"));
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
