package org.apereo.cas.notifications.push;

/**
 * This is {@link NotificationSenderExecutionPlanConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@FunctionalInterface
public interface NotificationSenderExecutionPlanConfigurer {
    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Configure notification sender notification sender.
     *
     * @return the notification sender
     */
    NotificationSender configureNotificationSender();
}
