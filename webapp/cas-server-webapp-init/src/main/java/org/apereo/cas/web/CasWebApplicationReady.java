package org.apereo.cas.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.metadata.CasConfigurationPropertiesValidator;
import org.apereo.cas.util.AsciiArtUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.spring.CasApplicationReadyListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import java.time.Instant;

/**
 * Application listener that gets invoked with the context gets ready.
 *
 * @author Hal Deadman
 * @since 6.6.0
 */
@Slf4j
@RequiredArgsConstructor
public class CasWebApplicationReady implements CasApplicationReadyListener {
    /**
     * System property to indicate whether configuration status has passed validation.
     */
    public static final String SYSTEM_PROPERTY_CONFIG_VALIDATION_STATUS = "CONFIG_VALIDATION_STATUS";
    private final ConfigurableApplicationContext applicationContext;
    private final CasConfigurationProperties properties;

    @Override
    public void handleApplicationReadyEvent(final ApplicationReadyEvent event) {
        AsciiArtUtils.printAsciiArtReady(LOGGER, "CAS is now running at " + properties.getServer().getPrefix());

        LOGGER.info("Ready to process requests @ [{}]", DateTimeUtils.zonedDateTimeOf(Instant.ofEpochMilli(event.getTimestamp())));
        val validator = new CasConfigurationPropertiesValidator(applicationContext);
        val results = validator.validate();
        System.setProperty(SYSTEM_PROPERTY_CONFIG_VALIDATION_STATUS, Boolean.toString(results.isEmpty()));
        validator.printReport(results);
    }
}
