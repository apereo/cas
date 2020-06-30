package org.apereo.cas.notifications.push;

import org.springframework.core.Ordered;

/**
 * This is {@link NotificationSender}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
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

}
