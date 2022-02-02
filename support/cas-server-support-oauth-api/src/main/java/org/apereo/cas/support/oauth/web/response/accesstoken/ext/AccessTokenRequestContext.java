package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import net.minidev.json.annotate.JsonIgnore;
import org.pac4j.core.profile.UserProfile;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link AccessTokenRequestContext}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ToString
@Getter
@SuperBuilder
@Jacksonized
public class AccessTokenRequestContext implements Serializable {

    private static final long serialVersionUID = 1428887534614525042L;

    private final Service service;

    @Nonnull
    private final Authentication authentication;

    private final OAuth20Token token;

    private final boolean generateRefreshToken;

    private final boolean expireOldRefreshToken;

    @Nonnull
    private final OAuthRegisteredService registeredService;

    @Setter
    private TicketGrantingTicket ticketGrantingTicket;

    @Builder.Default
    private final OAuth20GrantTypes grantType = OAuth20GrantTypes.NONE;

    @Builder.Default
    private final Set<String> scopes = new LinkedHashSet<>(0);

    @Builder.Default
    private final Map<String, Map<String, Object>> claims = new HashMap<>(0);

    @Builder.Default
    private final Map<String, Object> parameters = new HashMap<>(0);

    @Builder.Default
    private final OAuth20ResponseModeTypes responseMode = OAuth20ResponseModeTypes.NONE;

    @Builder.Default
    private final OAuth20ResponseTypes responseType = OAuth20ResponseTypes.NONE;

    private final String deviceCode;

    private final String codeChallenge;

    @Builder.Default
    private final String codeChallengeMethod = "plain";

    private final String codeVerifier;

    private final String clientId;

    private final String redirectUri;

    private final UserProfile userProfile;

    @JsonIgnore
    public boolean isCodeToken() {
        return token instanceof OAuth20Code;
    }

    @JsonIgnore
    public boolean isRefreshToken() {
        return token instanceof OAuth20RefreshToken;
    }
}
