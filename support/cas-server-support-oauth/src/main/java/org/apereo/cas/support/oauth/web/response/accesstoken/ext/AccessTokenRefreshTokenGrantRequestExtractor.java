package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.model.support.oauth.OAuthProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.registry.TicketRegistry;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link AccessTokenRefreshTokenGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class AccessTokenRefreshTokenGrantRequestExtractor extends AccessTokenAuthorizationCodeGrantRequestExtractor {


    public AccessTokenRefreshTokenGrantRequestExtractor(final ServicesManager servicesManager, final TicketRegistry ticketRegistry,
                                                        final CentralAuthenticationService centralAuthenticationService,
                                                        final OAuthProperties oAuthProperties,
                                                        final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory) {
        super(servicesManager, ticketRegistry, centralAuthenticationService, oAuthProperties, webApplicationServiceServiceFactory);
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

    @Override
    protected OAuthRegisteredService getOAuthRegisteredServiceBy(final HttpServletRequest request) {
        final String clientId = getRegisteredServiceIdentifierFromRequest(request);
        final OAuthRegisteredService registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId);
        LOGGER.debug("Located registered service [{}]", registeredService);
        return registeredService;
    }

    @Override
    protected String getRegisteredServiceIdentifierFromRequest(final HttpServletRequest request) {
        return request.getParameter(OAuth20Constants.CLIENT_ID);
    }
}
