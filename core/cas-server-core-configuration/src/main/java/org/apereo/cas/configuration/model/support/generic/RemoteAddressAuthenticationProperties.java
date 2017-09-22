package org.apereo.cas.configuration.model.support.generic;

import org.apache.commons.lang3.StringUtils;

/**
 * Configuration properties class for remote.authn.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
public class RemoteAddressAuthenticationProperties {

    private String ipAddressRange = StringUtils.EMPTY;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getIpAddressRange() {
        return ipAddressRange;
    }

    public void setIpAddressRange(final String ipAddressRange) {
        this.ipAddressRange = ipAddressRange;
    }
}
