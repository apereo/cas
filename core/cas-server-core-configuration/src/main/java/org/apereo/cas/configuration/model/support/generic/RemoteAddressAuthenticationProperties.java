package org.apereo.cas.configuration.model.support.generic;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;

import java.io.Serializable;

/**
 * Configuration properties class for remote.authn.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-generic-remote-webflow")
public class RemoteAddressAuthenticationProperties implements Serializable {

    private static final long serialVersionUID = 573409035023089696L;
    /**
     * The authorized network address to allow for authentication.
     */
    @RequiredProperty
    private String ipAddressRange = StringUtils.EMPTY;

    /**
     * The name of the authentication handler.
     */
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
