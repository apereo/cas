package org.apereo.cas.web;

import org.apereo.cas.util.AsciiArtUtils;
import org.apereo.cas.util.DateTimeUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.time.Instant;

/**
 * Application listener that gets invoked with the context gets ready.
 *
 * @author Hal Deadman
 * @since 6.6.0
 */
@Slf4j
public class CasWebApplicationReady implements CasWebApplicationReadyListener {

    @Override
    public void handleApplicationReadyEvent(final ApplicationReadyEvent event) {
        AsciiArtUtils.printAsciiArtReady(LOGGER, StringUtils.EMPTY);
        LOGGER.info("Ready to process requests @ [{}]", DateTimeUtils.zonedDateTimeOf(Instant.ofEpochMilli(event.getTimestamp())));
    }
}
