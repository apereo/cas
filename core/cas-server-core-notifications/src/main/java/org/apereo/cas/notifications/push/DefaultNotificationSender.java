package org.apereo.cas.notifications.push;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * This is {@link DefaultNotificationSender}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
public class DefaultNotificationSender implements NotificationSender {
    private final List<NotificationSender> notificationSenders;

    @Override
    public boolean canSend() {
        return notificationSenders.stream().anyMatch(NotificationSender::canSend);
    }
}
