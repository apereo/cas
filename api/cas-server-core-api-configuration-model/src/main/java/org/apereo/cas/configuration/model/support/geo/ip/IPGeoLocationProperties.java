package org.apereo.cas.configuration.model.support.geo.ip;

import org.apereo.cas.configuration.model.support.geo.BaseGeoLocationProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * This is {@link IPGeoLocationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiresModule(name = "cas-server-support-geolocation-ip")
@Getter
@Setter
@Accessors(chain = true)
public class IPGeoLocationProperties extends BaseGeoLocationProperties {

    @Serial
    private static final long serialVersionUID = 1883029275219817797L;

    /**
     * API key required for this integration.
     */
    @RequiredProperty
    private String apiKey;
}
