package org.apereo.cas.uma.web.controllers.authz;

import org.apereo.cas.support.oauth.util.OAuth20Utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * This is {@link UmaAuthorizationNeedInfoResponse}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Data
public class UmaAuthorizationNeedInfoResponse implements Serializable {
    private static final long serialVersionUID = -8719088128201373899L;

    @JsonProperty(value = "redirect_user", defaultValue = "true")
    private boolean redirectUser = true;

    @JsonProperty
    private String ticket;

    @JsonProperty("required_claims")
    private Collection<String> requiredClaims = new LinkedHashSet<>(0);

    @JsonProperty("required_scopes")
    private Collection<String> requiredScopes = new LinkedHashSet<>(0);

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
