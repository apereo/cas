package org.apereo.cas.util.io;

/**
 * This is {@link SmsSender}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface SmsSender {

    /**
     * Send sms to phone number.
     *
     * @param from    the from
     * @param to      the to
     * @param message the message
     * @return true/false
     */
    default boolean send(final String from, final String to, final String message) {
        return false;
    }

    /**
     * Whether it can send an SMS.
     *
     * @return whether it can send an SMS
     */
    default boolean canSend() {
        return true;
    }

    /**
     * No op sms sender.
     *
     * @return the sms sender
     */
    static SmsSender noOp() {
        return new SmsSender() {

            @Override
            public boolean canSend() {
                return false;
            }
        };
    }
}
