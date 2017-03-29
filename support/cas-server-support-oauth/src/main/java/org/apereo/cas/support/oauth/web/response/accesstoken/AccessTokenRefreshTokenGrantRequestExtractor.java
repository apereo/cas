package org.apereo.cas.support.oauth.web.response.accesstoken;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.OAuthToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link AccessTokenRefreshTokenGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class AccessTokenRefreshTokenGrantRequestExtractor extends AccessTokenAuthorizationCodeGrantRequestExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenRefreshTokenGrantRequestExtractor.class);

    public AccessTokenRefreshTokenGrantRequestExtractor(final ServicesManager servicesManager, final TicketRegistry ticketRegistry,
                                                        final HttpServletRequest request, final HttpServletResponse response) {
        super(servicesManager, ticketRegistry, request, response);
    }

    @Override
    protected OAuthToken getOAuthToken(final HttpServletRequest request) {
        return getOAuthTokenFromRequest(request, OAuthConstants.REFRESH_TOKEN);
    }

    /**
     * Supports the grant type?
     *
     * @param context the context
     * @return true/false
     */
    public static boolean supports(final HttpServletRequest context) {
        final String grantType = context.getParameter(OAuthConstants.GRANT_TYPE);
        return OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.REFRESH_TOKEN);
    }
}
