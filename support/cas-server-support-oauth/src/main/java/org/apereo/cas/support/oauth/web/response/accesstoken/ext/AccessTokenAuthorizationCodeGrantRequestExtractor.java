package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.model.support.oauth.OAuthProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.OAuthToken;
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
public class AccessTokenAuthorizationCodeGrantRequestExtractor extends BaseAccessTokenGrantRequestExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenAuthorizationCodeGrantRequestExtractor.class);

    public AccessTokenAuthorizationCodeGrantRequestExtractor(final ServicesManager servicesManager, final TicketRegistry ticketRegistry,
                                                             final HttpServletRequest request, final HttpServletResponse response,
                                                             final CentralAuthenticationService centralAuthenticationService,
                                                             final OAuthProperties oAuthProperties) {
        super(servicesManager, ticketRegistry, request, response, centralAuthenticationService, oAuthProperties);
    }

    @Override
    public AccessTokenRequestDataHolder extract() {
        final ProfileManager manager = WebUtils.getPac4jProfileManager(request, response);
        final String grantType = request.getParameter(OAuth20Constants.GRANT_TYPE);
        LOGGER.debug("OAuth grant type is [{}]", grantType);

        final Optional<UserProfile> profile = manager.get(true);
        final String clientId = profile.get().getId();
        final OAuthRegisteredService registeredService = OAuth20Utils.getRegisteredOAuthService(this.servicesManager, clientId);
        LOGGER.debug("Located OAuth registered service [{}]", registeredService);

        // we generate a refresh token if requested by the service but not from a refresh token
        final boolean generateRefreshToken = isAllowedToGenerateRefreshToken(registeredService);
        final OAuthToken token = getOAuthTokenFromRequest();
        if (token == null) {
            throw new InvalidTicketException(getOAuthParameter());
        }
        return new AccessTokenRequestDataHolder(token, generateRefreshToken, registeredService);
    }

    /**
     * Is allowed to generate refresh token ?
     *
     * @param registeredService the registered service
     * @return the boolean
     */
    protected boolean isAllowedToGenerateRefreshToken(final OAuthRegisteredService registeredService) {
        return registeredService != null && registeredService.isGenerateRefreshToken();
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
        if (token == null || token.isExpired()) {
            LOGGER.error("OAuth token indicated by parameter [{}] has expired or not found: [{}]", getOAuthParameter(), token);
            if (token != null) {
                this.ticketRegistry.deleteTicket(token.getId());
            }
            return null;
        }
        return token;
    }

    /**
     * Supports the grant type?
     *
     * @param context the context
     * @return true/false
     */
    @Override
    public boolean supports(final HttpServletRequest context) {
        final String grantType = context.getParameter(OAuth20Constants.GRANT_TYPE);
        return OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.AUTHORIZATION_CODE);
    }
}
