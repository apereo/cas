package org.apereo.cas.config;

import org.apereo.cas.notifications.BaseNotificationTests;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.notifications.push.NotificationSender;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasCoreNotificationsAutoConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseNotificationTests.SharedTestConfiguration.class, properties = {
    "spring.mail.host=localhost",
    "spring.mail.port=25000"
})
@Tag("Mail")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 25000)
class CasCoreNotificationsAutoConfigurationTests {
    @Autowired
    @Qualifier(NotificationSender.BEAN_NAME)
    private NotificationSender notificationSender;

    @Autowired
    @Qualifier(SmsSender.BEAN_NAME)
    private SmsSender smsSender;

    @Autowired
    @Qualifier(CommunicationsManager.BEAN_NAME)
    private CommunicationsManager communicationsManager;

    @Test
    void verifyOperation() {
        assertNotNull(notificationSender);
        assertNotNull(smsSender);
        assertNotNull(communicationsManager);
    }

}
