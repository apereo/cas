package org.apereo.cas.config;

import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.util.AsciiArtUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.time.Instant;

/**
 * This is {@link CasSpringBootAdminServerConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.SpringBootAdmin)
@AutoConfiguration
public class CasSpringBootAdminServerConfiguration {
    /**
     * Handle application ready event.
     *
     * @param event the event
     */
    @EventListener
    public void handleApplicationReadyEvent(final ApplicationReadyEvent event) {
        AsciiArtUtils.printAsciiArtReady(LOGGER, StringUtils.EMPTY);
        LOGGER.info("Ready to process requests @ [{}]", DateTimeUtils.zonedDateTimeOf(Instant.ofEpochMilli(event.getTimestamp())));
    }
}
