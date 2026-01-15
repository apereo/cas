package org.apereo.cas.oidc.assurance.entity;

import module java.base;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link Issuer}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
public class Issuer implements Serializable {
    @Serial
    private static final long serialVersionUID = 3472994858106103000L;
    
    private String name;

    private String country;
    
    private String region;

    @JsonProperty("country_code")
    private String countryCode;

    @JsonProperty("street_address")
    private String streetAddress;
    
    private String jurisdiction;
}
