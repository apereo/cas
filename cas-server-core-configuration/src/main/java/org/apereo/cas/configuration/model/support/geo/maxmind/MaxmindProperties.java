package org.apereo.cas.configuration.model.support.geo.maxmind;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/**
 * This is {@link MaxmindProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "geo.maxmind", ignoreUnknownFields = false)
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
