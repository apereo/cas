package org.apereo.cas.util.text;

/**
 * This is {@link MessageSanitizer}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@FunctionalInterface
public interface MessageSanitizer {
    /**
     * Default bean name.
     */
    String BEAN_NAME = "messageSanitizer";

    /**
     * Sanitize string.
     *
     * @param msg the msg
     * @return the string
     */
    String sanitize(String msg);

    /**
     * None message sanitizer.
     *
     * @return the message sanitizer
     */
    static MessageSanitizer disabled() {
        return msg -> msg;
    }
}
