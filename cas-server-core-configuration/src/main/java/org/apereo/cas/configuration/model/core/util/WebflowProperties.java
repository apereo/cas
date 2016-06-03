package org.apereo.cas.configuration.model.core.util;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties class for webflow.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "webflow", ignoreUnknownFields = false)
public class WebflowProperties extends AbstractCryptographyProperties {
}
