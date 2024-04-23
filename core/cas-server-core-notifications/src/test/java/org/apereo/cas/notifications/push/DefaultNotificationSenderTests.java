package org.apereo.cas.notifications.push;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.notifications.sms.SmsSender;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
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
    WebMvcAutoConfiguration.class,
    DefaultNotificationSenderTests.DefaultNotificationSenderTestConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class
})
@Tag("Simple")
class DefaultNotificationSenderTests {
    @Autowired
    @Qualifier("notificationSender")
    private NotificationSender notificationSender;

    @Test
    void verifyOperation() throws Throwable {
        assertTrue(notificationSender.canSend());
        assertFalse(new DefaultNotificationSender(List.of()).notify(CoreAuthenticationTestUtils.getPrincipal(), Map.of()));

        val sender = mock(NotificationSender.class);
        when(sender.getOrder()).thenCallRealMethod();
        assertEquals(Ordered.LOWEST_PRECEDENCE, sender.getOrder());

        val smsSender = mock(SmsSender.class);
        when(smsSender.send(anyString(), anyString(), anyString())).thenCallRealMethod();
        assertFalse(smsSender.send("1", "2", "3"));
    }

    @TestConfiguration(value = "DefaultNotificationSenderTestConfiguration", proxyBeanMethods = false)
    static class DefaultNotificationSenderTestConfiguration implements NotificationSenderExecutionPlanConfigurer {
        @Override
        public NotificationSender configureNotificationSender() {
            return NotificationSender.noOp();
        }
    }

}
