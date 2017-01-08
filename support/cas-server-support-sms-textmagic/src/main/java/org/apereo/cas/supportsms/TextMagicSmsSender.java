package org.apereo.cas.supportsms;

import org.apereo.cas.util.io.SmsSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link TextMagicSmsSender}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class TextMagicSmsSender implements SmsSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(TextMagicSmsSender.class);
    
    public TextMagicSmsSender() {
    }

    @Override
    public void send(final String from, final String to, final String message) {
        try {
            
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);        
        }
    }
}


