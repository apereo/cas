package org.apereo.cas.oidc.assurance.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link Source}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
public class Source implements Serializable {
    @Serial
    private static final long serialVersionUID = 1506639815280653068L;

    private String jurisdiction;

    @JsonProperty("country_code")
    private String countryCode;

    private String country;

    @JsonProperty("postal_code")
    private String postalCode;

    private String locality;

    @JsonProperty("street_address")
    private String streetAddress;

    private String name;
}
