package org.apereo.cas.configuration.metadata;

import lombok.experimental.UtilityClass;

/**
 * This is {@link Logger}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@UtilityClass
public class Logger {
    static void log(final String message, final Object... args) {
        //CHECKSTYLE:OFF
        System.out.printf(message, args);
        System.out.println();
        //CHECKSTYLE:ON
    }
}
