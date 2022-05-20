package org.apereo.cas.support.oauth.web.response.introspection;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link OAuth20IntrospectionAccessTokenResponse}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
public class OAuth20IntrospectionAccessTokenResponse {

	/**
	 * According to rfc7662 Introspection Response - active REQUIRED.
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7662#section-2.2"> Introspection Response</a>
	 */
    @JsonInclude(Include.ALWAYS)
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
