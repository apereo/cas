package org.apereo.cas.support.oauth.web.response.accesstoken;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.OAuthToken;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public AccessTokenRequestDataHolder(final OAuthToken token, final boolean generateRefreshToken,
                                        final OAuthRegisteredService registeredService) {
        this(token.getService(), token.getAuthentication(), token, generateRefreshToken, registeredService);
    }

    public AccessTokenRequestDataHolder(final Service service, final Authentication authentication, final OAuthToken token,
                                        final boolean generateRefreshToken, final OAuthRegisteredService registeredService) {
        this.service = service;
        this.authentication = authentication;
        this.token = token;
        this.generateRefreshToken = generateRefreshToken;
        this.registeredService = registeredService;
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
        return token != null ? token.getGrantingTicket() : null;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("service", service)
                .append("authentication", authentication)
                .append("token", token)
                .append("generateRefreshToken", generateRefreshToken)
                .append("registeredService", registeredService)
                .toString();
    }
}
