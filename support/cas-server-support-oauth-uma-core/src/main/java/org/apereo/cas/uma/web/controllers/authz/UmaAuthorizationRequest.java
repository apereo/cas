package org.apereo.cas.uma.web.controllers.authz;

import org.apereo.cas.support.oauth.util.OAuth20Utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * This is {@link UmaAuthorizationRequest}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Data
public class UmaAuthorizationRequest implements Serializable {
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
    public String toJson() {
        return OAuth20Utils.toJson(this);
    }
}
