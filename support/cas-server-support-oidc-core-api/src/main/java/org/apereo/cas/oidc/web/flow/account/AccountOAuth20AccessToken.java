package org.apereo.cas.oidc.web.flow.account;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link AccountOAuth20AccessToken}.
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
    private final Service service;
    private final String clientId;
    private final Set<String> scopes;

    AccountOAuth20AccessToken(final OAuth20AccessToken accessToken) {
        this.id = accessToken.getId();
        this.service = accessToken.getService();
        this.clientId = accessToken.getClientId();
        this.scopes = accessToken.getScopes();
    }
}
