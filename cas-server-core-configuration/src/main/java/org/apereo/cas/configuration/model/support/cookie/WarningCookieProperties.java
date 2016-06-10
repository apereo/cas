package org.apereo.cas.configuration.model.support.cookie;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties class for warn.cookie.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class WarningCookieProperties extends AbstractCookieProperties {

    public WarningCookieProperties() {
        super.setName("CASPRIVACY");
    }
}
