package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.oauth.OAuthProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.OAuthToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.Pac4jUtils;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link AccessTokenAuthorizationCodeGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class AccessTokenAuthorizationCodeGrantRequestExtractor extends BaseAccessTokenGrantRequestExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenAuthorizationCodeGrantRequestExtractor.class);

    public AccessTokenAuthorizationCodeGrantRequestExtractor(final ServicesManager servicesManager, final TicketRegistry ticketRegistry,
                                                             final CentralAuthenticationService centralAuthenticationService,
                                                             final OAuthProperties oAuthProperties) {
        super(servicesManager, ticketRegistry, centralAuthenticationService, oAuthProperties);
    }

    @Override
    public AccessTokenRequestDataHolder extract(final HttpServletRequest request, final HttpServletResponse response) {
        final String grantType = request.getParameter(OAuth20Constants.GRANT_TYPE);
        final Set<String> scopes = OAuth20Utils.parseRequestScopes(request);

        LOGGER.debug("OAuth grant type is [{}]", grantType);

        final ProfileManager manager = Pac4jUtils.getPac4jProfileManager(request, response);
        final Optional<UserProfile> profile = manager.get(true);
        final String clientId = profile.get().getId();
        final OAuthRegisteredService clientRegisteredService = OAuth20Utils.getRegisteredOAuthService(this.servicesManager, clientId);
        if (clientRegisteredService == null) {
            throw new UnauthorizedServiceException("Unable to locate OAuth service in registry for client " + clientId);
        }
        LOGGER.debug("Located OAuth client registered service [{}]", clientRegisteredService);

        final OAuthToken token = getOAuthTokenFromRequest(request);
        if (token == null) {
            throw new InvalidTicketException(getOAuthParameter(request));
        }
        final Service codeService = token.getService();
        final RegisteredService codeRegisteredService = OAuth20Utils.getRegisteredOAuthService(this.servicesManager, codeService.getId());
        LOGGER.debug("Located OAuth code registered service [{}]", codeRegisteredService);
        if (!clientRegisteredService.equals(codeRegisteredService)) {
            throw new UnauthorizedServiceException("Code and client registered services do not match");
        }
        return new AccessTokenRequestDataHolder(token, clientRegisteredService, getGrantType(), isAllowedToGenerateRefreshToken(), scopes);
    }

    /**
     * Is allowed to generate refresh token ?
     *
     * @return the boolean
     */
    protected boolean isAllowedToGenerateRefreshToken() {
        return true;
    }

    protected String getOAuthParameterName() {
        return OAuth20Constants.CODE;
    }

    /**
     * Gets o auth parameter.
     *
     * @param request the request
     * @return the o auth parameter
     */
    protected String getOAuthParameter(final HttpServletRequest request) {
        return request.getParameter(getOAuthParameterName());
    }

    /**
     * Return the OAuth token.
     *
     * @param request the request
     * @return the OAuth token
     */
    protected OAuthToken getOAuthTokenFromRequest(final HttpServletRequest request) {
        final OAuthToken token = this.ticketRegistry.getTicket(getOAuthParameter(request), OAuthToken.class);
        if (token == null || token.isExpired()) {
            LOGGER.error("OAuth token indicated by parameter [{}] has expired or not found: [{}]", getOAuthParameter(request), token);
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
        return OAuth20Utils.isGrantType(grantType, getGrantType());
    }

    @Override
    public OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.AUTHORIZATION_CODE;
    }
}
