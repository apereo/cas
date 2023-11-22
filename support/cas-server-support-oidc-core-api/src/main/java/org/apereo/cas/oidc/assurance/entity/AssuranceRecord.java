package org.apereo.cas.oidc.assurance.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link AssuranceRecord}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
public class AssuranceRecord implements Serializable {
    @Serial
    private static final long serialVersionUID = 7506639815280653068L;

    private String type;

    private Source source;

    @JsonProperty("personal_number")
    private String personalNumber;

    @JsonProperty("created_at")
    private String createdAt;
}
