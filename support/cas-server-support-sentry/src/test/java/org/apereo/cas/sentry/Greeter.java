package org.apereo.cas.sentry;

import org.apereo.cas.monitor.Monitorable;

/**
 * This is {@link Greeter}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@FunctionalInterface
@Monitorable(type = "Greeting")
public interface Greeter {
    /**
     * Greet object.
     *
     * @param fail the fail
     * @return the object
     */
    Object greet(boolean fail);

    /**
     * Default instance greeter.
     *
     * @return the greeter
     */
    static Greeter defaultInstance() {
        return new Greeter() {
            @Override
            public Object greet(final boolean fail) {
                if (fail) {
                    throw new IllegalArgumentException("Failed");
                }
                return "Hello";
            }
        };
    }
}
