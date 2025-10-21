package org.apereo.cas.notifications;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasAPNMessagingAutoConfiguration;
import org.apereo.cas.notifications.push.NotificationSender;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link APNMessagingNotificationSenderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@SpringBootTest(classes = {
    CasAPNMessagingAutoConfiguration.class,
    BaseNotificationTests.SharedTestConfiguration.class
}, properties = {
    "cas.apn-messaging.authentication-key.location=classpath:private-key.p8",
    "cas.apn-messaging.registration-token-attribute-name=registrationToken",
    "cas.apn-messaging.server=development",
    "cas.apn-messaging.team-id=1234567890",
    "cas.apn-messaging.key-id=1234567890",
    "cas.apn-messaging.topic=org.apereo.cas"
})
@Tag("Simple")
@ExtendWith(CasTestExtension.class)
class APNMessagingNotificationSenderTests {
    @Autowired
    @Qualifier(NotificationSender.BEAN_NAME)
    private NotificationSender notificationSender;
    
    @Test
    void verifyOperation() {
        val id = UUID.randomUUID().toString();
        val principal = CoreAuthenticationTestUtils.getPrincipal(Map.of("registrationToken", List.of(id)));
        assertDoesNotThrow(() -> {
            notificationSender.notify(principal, Map.of("title", "Hello", "message", "World"));
            Thread.sleep(2000);
        });
    }
}
