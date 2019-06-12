package org.apereo.cas.config;

import org.apereo.cas.util.AsciiArtUtils;
import org.apereo.cas.util.DateTimeUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * This is {@link CasSpringBootAdminServerConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@Configuration("casSpringBootAdminServerConfiguration")
public class CasSpringBootAdminServerConfiguration {
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
