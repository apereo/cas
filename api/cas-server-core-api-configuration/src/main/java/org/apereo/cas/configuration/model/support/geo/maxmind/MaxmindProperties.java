package org.apereo.cas.configuration.model.support.geo.maxmind;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.springframework.core.io.Resource;

import java.io.Serializable;

/**
 * This is {@link MaxmindProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-geolocation-maxmind")
public class MaxmindProperties implements Serializable {

    private static final long serialVersionUID = 7883029275219817797L;
    /**
     * Path to the location of the database file containing cities.
     */
    @RequiredProperty
    private Resource cityDatabase;
    /**
     * Path to the location of the database file containing countries.
     */
    @RequiredProperty
    private Resource countryDatabase;

    public Resource getCityDatabase() {
        return cityDatabase;
    }

    public void setCityDatabase(final Resource cityDatabase) {
        this.cityDatabase = cityDatabase;
    }

    public Resource getCountryDatabase() {
        return countryDatabase;
    }

    public void setCountryDatabase(final Resource countryDatabase) {
        this.countryDatabase = countryDatabase;
    }
}
