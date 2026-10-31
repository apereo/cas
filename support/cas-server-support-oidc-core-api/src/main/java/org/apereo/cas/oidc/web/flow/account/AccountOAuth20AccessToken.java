package org.apereo.cas.oidc.web.flow.account;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.ServiceAwareTicket;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link AccountOAuth20AccessToken}, an access token or a refresh token
 * (offline access) as shown in the account profile.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@Setter
@Accessors(chain = true)
class AccountOAuth20AccessToken implements Serializable {
    @Serial
    private static final long serialVersionUID = 5773773632522722494L;

    private final String id;
    private final @Nullable Service service;
    private final String clientId;
    private final Set<String> scopes;
    private final boolean refreshToken;
    private final int accessTokenCount;

    AccountOAuth20AccessToken(final OAuth20Token token) {
        this.id = token.getId();
        this.service = token instanceof final ServiceAwareTicket sat ? sat.getService() : null;
        this.clientId = token.getClientId();
        this.scopes = token.getScopes();
        this.refreshToken = token instanceof OAuth20RefreshToken;
        this.accessTokenCount = token instanceof final OAuth20RefreshToken rt ? rt.getAccessTokens().size() : 0;
    }
}
