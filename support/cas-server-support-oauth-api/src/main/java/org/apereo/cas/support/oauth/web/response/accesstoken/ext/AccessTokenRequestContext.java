package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.OAuth20TokenExchangeTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import net.minidev.json.annotate.JsonIgnore;
import org.pac4j.core.profile.UserProfile;
import java.io.Serial;
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
@With
@AllArgsConstructor
public class AccessTokenRequestContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 1428887534614525042L;

    private final Service service;

    private final Authentication authentication;

    private final Authentication actorToken;

    private final OAuth20Token token;

    private final boolean generateRefreshToken;

    private final boolean expireOldRefreshToken;

    private final OAuthRegisteredService registeredService;

    private final Ticket ticketGrantingTicket;

    @Builder.Default
    private final OAuth20GrantTypes grantType = OAuth20GrantTypes.NONE;

    @Builder.Default
    private final Set<String> scopes = new LinkedHashSet<>();

    @Builder.Default
    private final Map<String, Map<String, Object>> claims = new HashMap<>();

    @Builder.Default
    private final Map<String, Object> parameters = new HashMap<>();

    @Builder.Default
    private final OAuth20ResponseModeTypes responseMode = OAuth20ResponseModeTypes.NONE;

    @Builder.Default
    private final OAuth20ResponseTypes responseType = OAuth20ResponseTypes.NONE;

    private final String deviceCode;

    private final String codeChallenge;

    private final OAuth20TokenExchangeTypes subjectTokenType;

    private final OAuth20TokenExchangeTypes requestedTokenType;

    private final Serializable subjectToken;

    private final Service tokenExchangeResource;

    private final String tokenExchangeAudience;

    @Builder.Default
    private final String codeChallengeMethod = "plain";

    private final String codeVerifier;

    private final String clientId;

    private final String redirectUri;

    @Setter
    private UserProfile userProfile;

    @Setter
    private String dpopConfirmation;

    @Setter
    private String dpop;

    private final String cibaRequestId;
    
    @JsonIgnore
    public boolean isCodeToken() {
        return token instanceof OAuth20Code;
    }

    @JsonIgnore
    public boolean isRefreshToken() {
        return token instanceof OAuth20RefreshToken;
    }
}
