package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.OAuthToken;
import org.apereo.cas.ticket.TicketGrantingTicket;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is {@link AccessTokenRequestDataHolder}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@ToString
@Getter
public class AccessTokenRequestDataHolder {

    private Service service;

    private Authentication authentication;

    private OAuthToken token;

    private boolean generateRefreshToken;

    private OAuthRegisteredService registeredService;

    private TicketGrantingTicket ticketGrantingTicket;

    private OAuth20GrantTypes grantType;

    private Set<String> scopes;

    private OAuth20ResponseTypes responseType;

    private String deviceCode;

    public AccessTokenRequestDataHolder(final OAuthToken token, final OAuthRegisteredService registeredService, final OAuth20GrantTypes grantType,
                                        final boolean isAllowedToGenerateRefreshToken, final Set<String> scopes) {
        this(token.getService(), token.getAuthentication(), token, registeredService, grantType, isAllowedToGenerateRefreshToken, scopes);
    }

    public AccessTokenRequestDataHolder(final Service service, final Authentication authentication, final OAuthToken token,
                                        final OAuthRegisteredService registeredService, final OAuth20GrantTypes grantType,
                                        final boolean isAllowedToGenerateRefreshToken, final Set<String> scopes) {
        this(service, authentication, registeredService, token, null, grantType, isAllowedToGenerateRefreshToken, scopes);
    }

    public AccessTokenRequestDataHolder(final Service service, final Authentication authentication, final OAuthRegisteredService registeredService,
                                        final TicketGrantingTicket ticketGrantingTicket, final OAuth20GrantTypes grantType, final Set<String> scopes) {
        this(service, authentication, registeredService, null, ticketGrantingTicket, grantType, true, scopes);
    }

    private AccessTokenRequestDataHolder(final Service service, final Authentication authentication, final OAuthRegisteredService registeredService,
                                         final OAuthToken token, final TicketGrantingTicket ticketGrantingTicket, final OAuth20GrantTypes grantType,
                                         final boolean isAllowedToGenerateRefreshToken, final Set<String> scopes) {
        this.service = service;
        this.authentication = authentication;
        this.registeredService = registeredService;
        this.ticketGrantingTicket = token != null ? token.getTicketGrantingTicket() : ticketGrantingTicket;
        this.token = token;
        this.generateRefreshToken = isAllowedToGenerateRefreshToken ? (registeredService != null && registeredService.isGenerateRefreshToken()) : false;
        this.grantType = grantType;
        this.scopes = new LinkedHashSet<>(scopes);
    }

    public AccessTokenRequestDataHolder(final Service service, final OAuthRegisteredService registeredService,
                                        final OAuth20ResponseTypes responseType, final String deviceCode) {
        this.service = service;
        this.registeredService = registeredService;
        this.responseType = responseType;
        this.deviceCode = deviceCode;
    }
}
