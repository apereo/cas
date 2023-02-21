package org.apereo.cas.notifications.push;

import org.apereo.cas.authentication.principal.Principal;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

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

    @Override
    public boolean notify(final Principal principal, final Map<String, String> messageData) {
        return notificationSenders
            .stream()
            .anyMatch(sender -> {
                if (sender.canSend()) {
                    sender.notify(principal, messageData);
                    return true;
                }
                return false;
            });
    }
}
