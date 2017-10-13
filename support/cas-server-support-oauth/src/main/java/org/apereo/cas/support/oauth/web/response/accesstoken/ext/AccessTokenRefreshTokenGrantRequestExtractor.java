package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.model.support.oauth.OAuthProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link AccessTokenRefreshTokenGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class AccessTokenRefreshTokenGrantRequestExtractor extends AccessTokenAuthorizationCodeGrantRequestExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenRefreshTokenGrantRequestExtractor.class);

    public AccessTokenRefreshTokenGrantRequestExtractor(final ServicesManager servicesManager, final TicketRegistry ticketRegistry,
                                                        final CentralAuthenticationService centralAuthenticationService,
                                                        final OAuthProperties oAuthProperties) {
        super(servicesManager, ticketRegistry, centralAuthenticationService, oAuthProperties);
    }

    @Override
    protected String getOAuthParameterName() {
        return OAuth20Constants.REFRESH_TOKEN;
    }
    
    @Override
    protected boolean isAllowedToGenerateRefreshToken() {
        return false;
    }

    @Override
    public boolean supports(final HttpServletRequest context) {
        final String grantType = context.getParameter(OAuth20Constants.GRANT_TYPE);
        return OAuth20Utils.isGrantType(grantType, getGrantType());
    }

    @Override
    public OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.REFRESH_TOKEN;
    }
}
