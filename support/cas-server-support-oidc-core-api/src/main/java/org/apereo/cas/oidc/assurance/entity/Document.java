package org.apereo.cas.oidc.assurance.entity;

import module java.base;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link Document}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 * @deprecated Since 7.0.0
 */
@Getter
@Setter
@Deprecated(since = "7.0.0")
public class Document implements Serializable {
    @Serial
    private static final long serialVersionUID = -7502162711253545770L;

    private String type;

    private Issuer issuer;

    private String number;

    @JsonProperty("date_of_issuance")
    private String dateOfIssuance;

    @JsonProperty("date_of_expiry")
    private String dateOfExpiry;
}
