package org.apereo.cas.util.io;

/**
 * This is {@link SmsSender}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@FunctionalInterface
public interface SmsSender {

    /**
     * Send sms to phone number.
     *
     * @param from    the from
     * @param to      the to
     * @param message the message
     * @return the boolean
     */
    boolean send(String from, String to, String message);
}
