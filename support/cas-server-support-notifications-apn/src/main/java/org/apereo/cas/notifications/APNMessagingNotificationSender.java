package org.apereo.cas.notifications;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.apn.APNMessagingProperties;
import org.apereo.cas.notifications.push.NotificationSender;
import org.apereo.cas.util.LoggingUtils;
import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.util.SimpleApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import java.util.Map;

/**
 * This is {@link APNMessagingNotificationSender}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class APNMessagingNotificationSender implements NotificationSender {
    protected final ApnsClient apnsClient;
    protected final APNMessagingProperties properties;

    @Override
    @SuppressWarnings("FutureReturnValueIgnored")
    public boolean notify(final Principal principal, final Map<String, String> messageData) {
        val deviceToken = principal.getSingleValuedAttribute(properties.getRegistrationTokenAttributeName(), String.class);
        val payload = new SimpleApnsPayloadBuilder()
            .setAlertTitle(messageData.get("title"))
            .setAlertBody(messageData.get("message"))
            .build();

        val pushNotification = new SimpleApnsPushNotification(deviceToken, properties.getTopic(), payload);
        LOGGER.trace("Sending push notification to [{}] with payload [{}]", principal, pushNotification);

        apnsClient.sendNotification(pushNotification)
            .whenComplete((response, cause) -> {
                if (response != null) {
                    if (response.isAccepted()) {
                        LOGGER.debug("Notification [{}] sent to [{}] is accepted successfully", pushNotification, principal.getId());
                    } else {
                        LOGGER.warn("Notification [{}] sent to [{}] rejected with error: [{}]",
                            pushNotification, principal.getId(), response.getRejectionReason().orElse(StringUtils.EMPTY));
                    }
                } else {
                    LoggingUtils.error(LOGGER, cause);
                }
            });
        return true;
    }
}
