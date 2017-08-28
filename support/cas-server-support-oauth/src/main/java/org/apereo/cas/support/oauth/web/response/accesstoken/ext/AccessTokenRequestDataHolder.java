package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.OAuthToken;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is {@link AccessTokenRequestDataHolder}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class AccessTokenRequestDataHolder {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenRequestDataHolder.class);

    private final Service service;
    private final Authentication authentication;
    private final OAuthToken token;
    private final boolean generateRefreshToken;
    private final OAuthRegisteredService registeredService;
    private final TicketGrantingTicket ticketGrantingTicket;
    private final OAuth20GrantTypes grantType;
    private final Set<String> scopes;

    public AccessTokenRequestDataHolder(final OAuthToken token,
                                        final OAuthRegisteredService registeredService,
                                        final OAuth20GrantTypes grantType,
                                        final boolean isAllowedToGenerateRefreshToken,
                                        final Set<String> scopes) {
        this(token.getService(), token.getAuthentication(), token, registeredService,
                grantType, isAllowedToGenerateRefreshToken, scopes);
    }

    public AccessTokenRequestDataHolder(final Service service, final Authentication authentication,
                                        final OAuthToken token,
                                        final OAuthRegisteredService registeredService,
                                        final OAuth20GrantTypes grantType,
                                        final boolean isAllowedToGenerateRefreshToken,
                                        final Set<String> scopes) {
        this(service, authentication, registeredService, token, null,
                grantType, isAllowedToGenerateRefreshToken, scopes);
    }

    public AccessTokenRequestDataHolder(final Service service, final Authentication authentication,
                                        final OAuthRegisteredService registeredService,
                                        final TicketGrantingTicket ticketGrantingTicket,
                                        final OAuth20GrantTypes grantType,
                                        final Set<String> scopes) {
        this(service, authentication, registeredService, null,
                ticketGrantingTicket, grantType, true, scopes);
    }

    private AccessTokenRequestDataHolder(final Service service, final Authentication authentication,
                                         final OAuthRegisteredService registeredService,
                                         final OAuthToken token,
                                         final TicketGrantingTicket ticketGrantingTicket,
                                         final OAuth20GrantTypes grantType,
                                         final boolean isAllowedToGenerateRefreshToken,
                                         final Set<String> scopes) {
        this.service = service;
        this.authentication = authentication;
        this.registeredService = registeredService;
        this.ticketGrantingTicket = token != null ? token.getGrantingTicket() : ticketGrantingTicket;
        this.token = token;
        this.generateRefreshToken = isAllowedToGenerateRefreshToken ? (registeredService != null && registeredService.isGenerateRefreshToken()) : false;
        this.grantType = grantType;
        this.scopes = new LinkedHashSet<>(scopes);
    }

    public OAuth20GrantTypes getGrantType() {
        return grantType;
    }

    public Service getService() {
        return service;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public OAuthToken getToken() {
        return token;
    }

    public boolean isGenerateRefreshToken() {
        return generateRefreshToken;
    }

    public OAuthRegisteredService getRegisteredService() {
        return registeredService;
    }

    public TicketGrantingTicket getTicketGrantingTicket() {
        return this.ticketGrantingTicket;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("service", service)
                .append("authentication", authentication)
                .append("token", token)
                .append("generateRefreshToken", generateRefreshToken)
                .append("registeredService", registeredService)
                .append("ticketGrantingTicket", ticketGrantingTicket)
                .append("grantType", grantType)
                .append("scopes", scopes)
                .toString();
    }
}
