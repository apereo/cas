package org.apereo.cas.support.oauth.web.response;

import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;

/**
 * This is {@link OAuth20AuthorizationRequest}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SuperBuilder
@Getter
@Jacksonized
public class OAuth20AuthorizationRequest implements Serializable {
    private static final long serialVersionUID = 3119467088485455394L;

    private final String responseType;

    private final String grantType;

    private final String url;

    private final String clientId;

    private final AccessTokenRequestContext accessTokenRequest;

    /**
     * Is single sign on session required for this request?
     * This generally forces the presence of a ticket-granting ticket
     * to be found before this builder can operate further.
     * Some builders may be able to work without a session initially,
     * such as those that operate on PAR requests.
     */
    @Builder.Default
    private final boolean singleSignOnSessionRequired = true;
}
