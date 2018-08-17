package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.OAuthToken;
import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is {@link AccessTokenRequestDataHolder}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ToString
@Getter
@Builder
public class AccessTokenRequestDataHolder {

    private Service service;

    private Authentication authentication;

    private OAuthToken token;

    private boolean generateRefreshToken;

    private OAuthRegisteredService registeredService;

    private TicketGrantingTicket ticketGrantingTicket;

    @Builder.Default
    private OAuth20GrantTypes grantType = OAuth20GrantTypes.NONE;

    @Builder.Default
    private Set<String> scopes = new LinkedHashSet<>();

    @Builder.Default
    private OAuth20ResponseTypes responseType = OAuth20ResponseTypes.NONE;

    private String deviceCode;

    private String codeChallenge;

    private String codeChallengeMethod = "plain";

    private String codeVerifier;
}
