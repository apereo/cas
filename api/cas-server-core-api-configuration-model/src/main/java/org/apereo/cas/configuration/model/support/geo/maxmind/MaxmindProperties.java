package org.apereo.cas.configuration.model.support.geo.maxmind;

import org.apereo.cas.configuration.model.support.geo.BaseGeoLocationProperties;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.core.io.Resource;

import java.io.Serial;

/**
 * This is {@link MaxmindProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-geolocation-maxmind")
@Getter
@Setter
@Accessors(chain = true)
public class MaxmindProperties extends BaseGeoLocationProperties {

    @Serial
    private static final long serialVersionUID = 7883029275219817797L;

    /**
     * Geolocating an IP address using Maxmind web services
     * will need your MaxMind account ID and license key.
     * A zero or negative value will deactivate the web services integration.
     */
    private int accountId;

    /**
     * Geolocating an IP address using Maxmind web services
     * will need your MaxMind account ID and license key.
     * A blank, undefined value will deactivate the web services integration.
     */
    private String licenseKey;
    
    /**
     * Path to the location of the database file containing cities.
     */
    @RequiredProperty
    private transient Resource cityDatabase;

    /**
     * Path to the location of the database file containing countries.
     */
    @RequiredProperty
    private transient Resource countryDatabase;
}
