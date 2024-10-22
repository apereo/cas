package org.apereo.cas.notifications.sms;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * This is {@link MockSmsSender}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MockSmsSender implements SmsSender {
    public static final SmsSender INSTANCE = new MockSmsSender();

    @Override
    public boolean send(final String from, final String to, final String message) {
        return true;
    }

    public static SmsSender withMessage(final String s) {
        return new SmsSender() {
            @Override
            public boolean send(final String from, final String to, final String message) {
                return message.matches(s);
            }
        };
    }
}
