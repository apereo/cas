package org.apereo.cas.notifications.sms;

/**
 * This is {@link MockSmsSender}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class MockSmsSender implements SmsSender {
    @Override
    public boolean send(final String from, final String to, final String message) {
        return true;
    }
}
