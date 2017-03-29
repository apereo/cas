package org.apereo.cas.support.oauth.web.response.accesstoken;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.authenticator.OAuth20CasAuthenticationBuilder;
import org.apereo.cas.support.oauth.profile.OAuthUserProfile;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * This is {@link AccessTokenPasswordGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class AccessTokenPasswordGrantRequestExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessTokenPasswordGrantRequestExtractor.class);

    private final ServicesManager servicesManager;
    private final TicketRegistry ticketRegistry;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final OAuth20CasAuthenticationBuilder authenticationBuilder;

    public AccessTokenPasswordGrantRequestExtractor(final ServicesManager servicesManager, final TicketRegistry ticketRegistry,
                                                    final HttpServletRequest request, final HttpServletResponse response,
                                                    final OAuth20CasAuthenticationBuilder authenticationBuilder) {
        this.servicesManager = servicesManager;
        this.ticketRegistry = ticketRegistry;
        this.request = request;
        this.response = response;
        this.authenticationBuilder = authenticationBuilder;
    }

    /**
     * Extract access token request data holder.
     *
     * @return the access token request data holder
     */
    public AccessTokenRequestDataHolder extract() {
        final String clientId = request.getParameter(OAuthConstants.CLIENT_ID);
        LOGGER.debug("Locating OAuth registered service by client id [{}]", clientId);

        final OAuthRegisteredService registeredService = OAuth20Utils.getRegisteredOAuthService(this.servicesManager, clientId);
        LOGGER.debug("Located OAuth registered service [{}]", registeredService);
        final boolean generateRefreshToken = registeredService != null && registeredService.isGenerateRefreshToken();

        final J2EContext context = WebUtils.getPac4jJ2EContext(request, response);
        final ProfileManager manager = WebUtils.getPac4jProfileManager(request, response);
        final Optional<OAuthUserProfile> profile = manager.get(true);
        if (!profile.isPresent()) {
            throw new UnauthorizedServiceException("OAuth user profile cannot be determined");
        }
        LOGGER.debug("Creating matching service request based on [{}]", registeredService);
        final Service service = this.authenticationBuilder.buildService(registeredService, context);

        LOGGER.debug("Authenticating the OAuth request indicated by [{}]", service);
        final Authentication authentication = this.authenticationBuilder.build(profile.get(), registeredService, context);
        RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service, registeredService, authentication);
        return new AccessTokenRequestDataHolder(service, authentication, null, generateRefreshToken, registeredService);
    }

    /**
     * Supports the grant type?
     *
     * @param context the context
     * @return true/false
     */
    public static boolean supports(final HttpServletRequest context) {
        final String grantType = context.getParameter(OAuthConstants.GRANT_TYPE);
        return OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.PASSWORD);
    }
}
