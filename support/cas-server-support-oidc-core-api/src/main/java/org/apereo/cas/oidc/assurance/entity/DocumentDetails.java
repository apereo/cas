package org.apereo.cas.oidc.assurance.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link DocumentDetails}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
public class DocumentDetails implements Serializable {
    @Serial
    private static final long serialVersionUID = -7102162711253545770L;

    private String type;
    
    @JsonProperty("personal_number")
    private String personalNumber;

    @JsonProperty("document_number")
    private String documentNumber;

    @JsonProperty("serial_number")
    private String serialNumber;

    @JsonProperty("date_of_issuance")
    private String dateOfIssuance;

    @JsonProperty("date_of_expiry")
    private String dateOfExpiry;

    private Issuer issuer;
}
