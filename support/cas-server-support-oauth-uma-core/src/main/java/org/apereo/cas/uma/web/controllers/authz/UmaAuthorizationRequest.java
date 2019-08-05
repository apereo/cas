package org.apereo.cas.uma.web.controllers.authz;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;

import java.io.Serializable;

/**
 * This is {@link UmaAuthorizationRequest}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Data
public class UmaAuthorizationRequest implements Serializable {
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .findAndRegisterModules();

    private static final long serialVersionUID = -5359723510084259980L;

    @JsonProperty
    private String ticket;

    @JsonProperty
    private String rpt;

    @JsonProperty("grant_type")
    private String grantType;

    @JsonProperty("claim_token")
    private String claimToken;

    @JsonProperty("claim_token_format")
    private String claimTokenFormat;

    /**
     * As json string.
     *
     * @return the string
     */
    @JsonIgnore
    @SneakyThrows
    public String toJson() {
        return MAPPER.writeValueAsString(this);
    }
}
