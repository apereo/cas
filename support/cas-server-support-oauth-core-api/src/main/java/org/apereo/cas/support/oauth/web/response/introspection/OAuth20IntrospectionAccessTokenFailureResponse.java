package org.apereo.cas.support.oauth.web.response.introspection;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * This is {@link OAuth20IntrospectionAccessTokenFailureResponse}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
public class OAuth20IntrospectionAccessTokenFailureResponse extends BaseOAuth20IntrospectionAccessTokenResponse {
    @Serial
    private static final long serialVersionUID = -7917281748569741345L;

    private String error;
}
