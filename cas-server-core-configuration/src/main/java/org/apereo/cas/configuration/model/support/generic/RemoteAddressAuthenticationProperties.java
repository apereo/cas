package org.apereo.cas.configuration.model.support.generic;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties class for remote.authn.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "remote.authn", ignoreUnknownFields = false)
public class RemoteAddressAuthenticationProperties {

    private String ipAddressRange = "";

    public String getIpAddressRange() {
        return ipAddressRange;
    }

    public void setIpAddressRange(final String ipAddressRange) {
        this.ipAddressRange = ipAddressRange;
    }
}
