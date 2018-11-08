package org.apereo.cas.oidc.introspection;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link OidcIntrospectionAccessTokenResponse}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
public class OidcIntrospectionAccessTokenResponse {

    private boolean active;

    private String sub;

    private String scope;

    private long iat;

    private long exp;

    private String realmName;

    private String uniqueSecurityName;

    private String tokenType;

    private String aud;

    private String iss;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("grant_type")
    private String grantType;
}
