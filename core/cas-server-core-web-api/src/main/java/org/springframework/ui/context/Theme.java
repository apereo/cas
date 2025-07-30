package org.springframework.ui.context;

import org.springframework.context.MessageSource;

/**
 * This is {@link Theme}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public interface Theme {
    /**
     * Gets name.
     *
     * @return the name
     */
    String getName();

    /**
     * Gets message source.
     *
     * @return the message source
     */
    MessageSource getMessageSource();
}
