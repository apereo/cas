package org.apereo.cas.configuration.model.core.util;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties class for ticket.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "ticket", ignoreUnknownFields = false)
public class TicketProperties extends AbstractCryptographyProperties {
}
