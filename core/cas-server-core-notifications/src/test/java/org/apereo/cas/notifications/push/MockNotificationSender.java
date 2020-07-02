package org.apereo.cas.notifications.push;

/**
 * This is {@link MockNotificationSender}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class MockNotificationSender implements NotificationSender {
    @Override
    public boolean canSend() {
        return true;
    }
}
