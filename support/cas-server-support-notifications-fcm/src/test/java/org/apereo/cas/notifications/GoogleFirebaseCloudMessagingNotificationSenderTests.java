package org.apereo.cas.notifications;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.GoogleFirebaseCloudMessagingConfiguration;
import org.apereo.cas.notifications.push.NotificationSender;

import com.google.common.io.Files;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GoogleFirebaseCloudMessagingNotificationSenderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    GoogleFirebaseCloudMessagingConfiguration.class,
    CasCoreUtilConfiguration.class
}, properties = {
    "cas.google-firebase-messaging.service-account-key.location=file:/tmp/account-key.json",
    "cas.google-firebase-messaging.database-url=https://cassso-2531381995058.firebaseio.com",
    "cas.google-firebase-messaging.registration-token-attribute-name=registrationToken"
})
@Tag("Simple")
public class GoogleFirebaseCloudMessagingNotificationSenderTests {

    @Autowired
    @Qualifier("firebaseCloudMessagingNotificationSender")
    private NotificationSender firebaseCloudMessagingNotificationSender;

    @Autowired
    @Qualifier("notificationSender")
    private NotificationSender notificationSender;

    @BeforeAll
    public static void beforeAll() throws Exception {
        val key = IOUtils.toString(new ClassPathResource("account-key.json").getInputStream(), StandardCharsets.UTF_8);
        try (val writer = Files.newWriter(new File("/tmp/account-key.json"), StandardCharsets.UTF_8)) {
            IOUtils.write(key, writer);
            writer.flush();
        }
    }

    @Test
    public void verifyOperation() {
        assertNotNull(firebaseCloudMessagingNotificationSender);
        assertNotNull(notificationSender);
        val id = UUID.randomUUID().toString();
        val principal = CoreAuthenticationTestUtils.getPrincipal(Map.of("registrationToken", List.of(id)));
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                notificationSender.notify(principal, Map.of("title", "Hello", "message", "World"));
            }
        });
    }

}
