package org.apereo.cas.support.oauth.web.response.accesstoken;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.OAuthToken;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * This is {@link AccessTokenAuthorizationCodeGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class AccessTokenAuthorizationCodeGrantRequestExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenAuthorizationCodeGrantRequestExtractor.class);

    private final ServicesManager servicesManager;
    private final TicketRegistry ticketRegistry;
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    public AccessTokenAuthorizationCodeGrantRequestExtractor(final ServicesManager servicesManager, final TicketRegistry ticketRegistry,
                                                             final HttpServletRequest request, final HttpServletResponse response) {
        this.servicesManager = servicesManager;
        this.ticketRegistry = ticketRegistry;
        this.request = request;
        this.response = response;
    }

    /**
     * Extract access token request data holder.
     *
     * @return the access token request data holder
     */
    public AccessTokenRequestDataHolder extract() {
        final ProfileManager manager = WebUtils.getPac4jProfileManager(request, response);
        final String grantType = request.getParameter(OAuth20Constants.GRANT_TYPE);
        LOGGER.debug("OAuth grant type is [{}]", grantType);

        final Optional<UserProfile> profile = manager.get(true);
        final String clientId = profile.get().getId();
        final OAuthRegisteredService registeredService = OAuth20Utils.getRegisteredOAuthService(this.servicesManager, clientId);
        LOGGER.debug("Located OAuth registered service [{}]", registeredService);

        // we generate a refresh token if requested by the service but not from a refresh token
        final boolean generateRefreshToken = registeredService != null && registeredService.isGenerateRefreshToken();
        final OAuthToken token = getOAuthTokenFromRequest();
        if (token == null) {
            throw new InvalidTicketException(getOAuthParameter());
        }
        return new AccessTokenRequestDataHolder(token, generateRefreshToken, registeredService);
    }

    protected String getOAuthParameterName() {
        return OAuth20Constants.CODE;
    }

    protected String getOAuthParameter() {
        return request.getParameter(getOAuthParameterName());
    }

    /**
     * Return the OAuth token.
     *
     * @return the OAuth token
     */
    protected OAuthToken getOAuthTokenFromRequest() {
        final OAuthToken token = this.ticketRegistry.getTicket(getOAuthParameter(), OAuthToken.class);
        // token should not be expired
        if (token == null || token.isExpired()) {
            LOGGER.error("Code or refresh token expired: [{}]", token);
            if (token != null) {
                this.ticketRegistry.deleteTicket(token.getId());
            }
            return null;
        }
        if (token instanceof OAuthCode && !(token instanceof RefreshToken)) {
            this.ticketRegistry.deleteTicket(token.getId());
        }
        return token;
    }

    /**
     * Supports the grant type?
     *
     * @param context the context
     * @return true/false
     */
    public static boolean supports(final HttpServletRequest context) {
        final String grantType = context.getParameter(OAuth20Constants.GRANT_TYPE);
        return OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.AUTHORIZATION_CODE);
    }
}
