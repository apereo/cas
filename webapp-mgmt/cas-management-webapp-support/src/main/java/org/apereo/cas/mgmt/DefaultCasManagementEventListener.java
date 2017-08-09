package org.apereo.cas.mgmt;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.util.AsciiArtUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * This is {@link DefaultCasManagementEventListener}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DefaultCasManagementEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCasManagementEventListener.class);
    
    /**
     * Handle application ready event.
     *
     * @param event the event
     */
    @EventListener
    public void handleApplicationReadyEvent(final ApplicationReadyEvent event) {
        AsciiArtUtils.printAsciiArtInfo(LOGGER, "READY", StringUtils.EMPTY);
        LOGGER.info("Ready to process requests @ [{}]", DateTimeUtils.zonedDateTimeOf(event.getTimestamp()));
    }
}
