package org.apereo.cas.oidc.assurance.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link Attestation}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
public class Attestation implements Serializable {
    @Serial
    private static final long serialVersionUID = -110227536920377038L;

    private String type;

    @JsonProperty("reference_number")
    private String referenceNumber;

    @JsonProperty("date_of_issuance")
    private String dateOfIssuance;
}
