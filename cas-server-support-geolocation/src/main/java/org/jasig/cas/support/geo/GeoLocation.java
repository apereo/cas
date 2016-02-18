package org.jasig.cas.support.geo;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This is {@link GeoLocation} that represents a particular geo location
 * usually calculated from an ip address.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public class GeoLocation {
    private String city;
    private String country;

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) {
        this.country = country;
    }


    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final GeoLocation rhs = (GeoLocation) obj;
        return new EqualsBuilder()
                .append(this.city, rhs.city)
                .append(this.country, rhs.country)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(city)
                .append(country)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("city", city)
                .append("country", country)
                .toString();
    }
}
