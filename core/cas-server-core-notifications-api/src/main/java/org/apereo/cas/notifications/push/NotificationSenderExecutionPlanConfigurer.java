package org.apereo.cas.notifications.push;

import module java.base;
import org.apereo.cas.util.NamedObject;

/**
 * This is {@link NotificationSenderExecutionPlanConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@FunctionalInterface
public interface NotificationSenderExecutionPlanConfigurer extends NamedObject {

    /**
     * Configure notification sender notification sender.
     *
     * @return the notification sender
     */
    NotificationSender configureNotificationSender();
}
