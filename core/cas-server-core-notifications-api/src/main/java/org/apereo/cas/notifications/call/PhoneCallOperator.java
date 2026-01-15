package org.apereo.cas.notifications.call;
import module java.base;

/**
 * This is {@link PhoneCallOperator}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface PhoneCallOperator {
    /**
     * Bean name.
     */
    String BEAN_NAME = "phoneCallOperator";

    /**
     * No op phone call operator.
     *
     * @return the phone call operator
     */
    static PhoneCallOperator noOp() {
        return new PhoneCallOperator() {
            @Override
            public boolean canCall() {
                return false;
            }
        };
    }

    /**
     * Call.
     *
     * @param from    the from
     * @param to      the to
     * @param message the message
     * @return true/false
     * @throws Throwable the throwable
     */
    default boolean call(final String from, final String to, final String message) throws Throwable {
        return false;
    }

    /**
     * Determine if a phone call can be made.
     *
     * @return true/false
     */
    default boolean canCall() {
        return true;
    }
}
