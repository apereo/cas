package org.apereo.cas.configuration.model.support.geo.maxmind;

import org.springframework.core.io.Resource;

/**
 * This is {@link MaxmindProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class MaxmindProperties {
    
    private Resource cityDatabase;
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
