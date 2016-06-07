package org.apereo.cas.configuration.model.support.generic;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties class for ip.address.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "ip.address", ignoreUnknownFields = false)
public class RemoteAddressAuthenticationProperties {

    private String range = "";

    public String getRange() {
        return range;
    }

    public void setRange(final String range) {
        this.range = range;
    }
}
