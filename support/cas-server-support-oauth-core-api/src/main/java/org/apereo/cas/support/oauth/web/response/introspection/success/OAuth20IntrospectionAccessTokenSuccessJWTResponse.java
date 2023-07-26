package org.apereo.cas.support.oauth.web.response.introspection.success;

import org.apereo.cas.support.oauth.web.response.introspection.BaseOAuth20IntrospectionAccessTokenResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.io.Serial;

/**
 * This is {@link OAuth20IntrospectionAccessTokenSuccessJWTResponse}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
public class OAuth20IntrospectionAccessTokenSuccessJWTResponse extends BaseOAuth20IntrospectionAccessTokenResponse {
    @Serial
    private static final long serialVersionUID = -7927281748569741345L;

    private long iat;

    private String aud;

    private String iss;
    
    @JsonProperty("token_introspection")
    private OAuth20IntrospectionAccessTokenSuccessResponse tokenIntrospection;
}
