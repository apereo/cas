package org.apereo.cas.notifications;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.firebase.GoogleFirebaseCloudMessagingProperties;
import org.apereo.cas.notifications.push.NotificationSender;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;

import java.util.Map;

/**
 * This is {@link GoogleFirebaseCloudMessagingNotificationSender}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class GoogleFirebaseCloudMessagingNotificationSender implements NotificationSender {
    private final GoogleFirebaseCloudMessagingProperties properties;

    @Override
    public void notify(final Principal principal, final Map<String, String> messageData) {
        try {
            CollectionUtils.firstElement(principal.getAttributes().get(properties.getRegistrationTokenAttributeName()))
                .ifPresent(Unchecked.consumer(token -> {
                    val message = Message.builder().putAllData(messageData)
                        .setToken(token.toString())
                        .build();
                    FirebaseMessaging.getInstance().send(message);
                }));
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
    }
}
