package org.apereo.cas.support.oauth.web.response;

import module java.base;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

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
    @Serial
    private static final long serialVersionUID = 3119467088485455394L;

    private final String responseType;

    private final String grantType;

    private final String url;

    private final String clientId;

    private final AccessTokenRequestContext accessTokenRequest;

    private final String authorizationDetails;

    private final String issuerState;

    private final OAuthRegisteredService registeredService;

    private final String codeChallenge;

    /**
     * Determines whether this request requires a single sign-on session.
     * This generally forces the presence of a ticket-granting ticket
     * to be found before this builder can operate further.
     * Some builders may be able to work without a session initially,
     * such as those that operate on PAR requests.
     */
    @Builder.Default
    private final boolean singleSignOnSessionRequired = true;
}
