package org.apereo.cas.notifications;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasGoogleFirebaseCloudMessagingAutoConfiguration;
import org.apereo.cas.notifications.push.NotificationSender;
import org.apereo.cas.test.CasTestExtension;
import com.google.common.io.Files;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GoogleFirebaseCloudMessagingNotificationSenderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = {
    CasGoogleFirebaseCloudMessagingAutoConfiguration.class,
    BaseNotificationTests.SharedTestConfiguration.class
}, properties = {
    "cas.google-firebase-messaging.service-account-key.location=file:${java.io.tmpdir}/account-key.json",
    "cas.google-firebase-messaging.database-url=https://cassso-2531381995058.firebaseio.com",
    "cas.google-firebase-messaging.registration-token-attribute-name=registrationToken"
})
@Tag("Simple")
@ExtendWith(CasTestExtension.class)
class GoogleFirebaseCloudMessagingNotificationSenderTests {

    @Autowired
    @Qualifier("firebaseCloudMessagingNotificationSender")
    private NotificationSender firebaseCloudMessagingNotificationSender;

    @Autowired
    @Qualifier(NotificationSender.BEAN_NAME)
    private NotificationSender notificationSender;

    @BeforeAll
    public static void beforeAll() throws Exception {
        val key = IOUtils.toString(new ClassPathResource("account-key.json").getInputStream(), StandardCharsets.UTF_8);
        try (val writer = Files.newWriter(
                new File(FileUtils.getTempDirectory(), "account-key.json"), StandardCharsets.UTF_8)) {
            IOUtils.write(key, writer);
            writer.flush();
        }
    }

    @Test
    void verifyOperation() {
        assertNotNull(firebaseCloudMessagingNotificationSender);
        assertNotNull(notificationSender);
        val id = UUID.randomUUID().toString();
        val principal = CoreAuthenticationTestUtils.getPrincipal(Map.of("registrationToken", List.of(id)));
        assertDoesNotThrow(() -> {
            notificationSender.notify(principal, Map.of("title", "Hello", "message", "World"));
        });
    }

}
