package org.apereo.cas.notifications.push;

import org.apereo.cas.authentication.principal.Principal;

import org.springframework.core.Ordered;

import java.util.Map;

/**
 * This is {@link NotificationSender}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@FunctionalInterface
public interface NotificationSender extends Ordered {

    /**
     * Bean name for the notification sender.
     */
    String BEAN_NAME = "notificationSender";

    /**
     * Attribute name in the payload that indicates the message title.
     */
    String ATTRIBUTE_NOTIFICATION_TITLE = "title";
    /**
     * Attribute name in the payload that indicates the message body.
     */
    String ATTRIBUTE_NOTIFICATION_MESSAGE = "message";

    /**
     * No op notification sender.
     *
     * @return the notification sender
     */
    static NotificationSender noOp() {
        return (principal, messageData) -> true;
    }

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * Whether it can send a notification.
     *
     * @return whether it can send an notification
     */
    default boolean canSend() {
        return true;
    }

    /**
     * Notify.
     *
     * @param principal   the principal
     * @param messageData the message data
     * @return true/false
     */
    boolean notify(Principal principal, Map<String, String> messageData);
}
