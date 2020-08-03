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

    /**
     * No op notification sender.
     *
     * @return the notification sender
     */
    static NotificationSender noOp() {
        return (principal, messageData) -> true;
    }
}
