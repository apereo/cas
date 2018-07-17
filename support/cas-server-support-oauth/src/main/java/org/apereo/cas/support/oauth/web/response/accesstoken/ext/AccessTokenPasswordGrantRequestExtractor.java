package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.DefaultAuthenticationResult;
import org.apereo.cas.configuration.model.support.oauth.OAuthProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.authenticator.OAuth20CasAuthenticationBuilder;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.Pac4jUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.profile.CommonProfile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * This is {@link AccessTokenPasswordGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class AccessTokenPasswordGrantRequestExtractor extends BaseAccessTokenGrantRequestExtractor {

    private final AuditableExecution registeredServiceAccessStrategyEnforcer;
    private final OAuth20CasAuthenticationBuilder authenticationBuilder;

    public AccessTokenPasswordGrantRequestExtractor(final ServicesManager servicesManager,
                                                    final TicketRegistry ticketRegistry,
                                                    final OAuth20CasAuthenticationBuilder authenticationBuilder,
                                                    final CentralAuthenticationService centralAuthenticationService,
                                                    final OAuthProperties oAuthProperties,
                                                    final AuditableExecution registeredServiceAccessStrategyEnforcer) {
        super(servicesManager, ticketRegistry, centralAuthenticationService, oAuthProperties);
        this.authenticationBuilder = authenticationBuilder;
        this.registeredServiceAccessStrategyEnforcer = registeredServiceAccessStrategyEnforcer;
    }

    @Override
    public AccessTokenRequestDataHolder extract(final HttpServletRequest request, final HttpServletResponse response) {
        val clientId = request.getParameter(OAuth20Constants.CLIENT_ID);
        val scopes = OAuth20Utils.parseRequestScopes(request);
        LOGGER.debug("Locating OAuth registered service by client id [{}]", clientId);

        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId);
        LOGGER.debug("Located OAuth registered service [{}]", registeredService);

        val context = Pac4jUtils.getPac4jJ2EContext(request, response);
        val manager = Pac4jUtils.getPac4jProfileManager(request, response);
        val profile = (Optional<CommonProfile>) manager.get(true);
        if (!profile.isPresent()) {
            throw new UnauthorizedServiceException("OAuth user profile cannot be determined");
        }
        val uProfile = profile.get();
        LOGGER.debug("Creating matching service request based on [{}]", registeredService);
        val requireServiceHeader = oAuthProperties.getGrants().getResourceOwner().isRequireServiceHeader();
        if (requireServiceHeader) {
            LOGGER.debug("Using request headers to identify and build the target service url");
        }
        val service = this.authenticationBuilder.buildService(registeredService, context, requireServiceHeader);

        LOGGER.debug("Authenticating the OAuth request indicated by [{}]", service);
        val authentication = this.authenticationBuilder.build(uProfile, registeredService, context, service);

        val audit = AuditableContext.builder()
            .service(service)
            .authentication(authentication)
            .registeredService(registeredService)
            .retrievePrincipalAttributesFromReleasePolicy(Boolean.TRUE)
            .build();
        val accessResult = this.registeredServiceAccessStrategyEnforcer.execute(audit);
        accessResult.throwExceptionIfNeeded();

        val result = new DefaultAuthenticationResult(authentication, requireServiceHeader ? service : null);
        val ticketGrantingTicket = this.centralAuthenticationService.createTicketGrantingTicket(result);

        return AccessTokenRequestDataHolder.builder()
            .scopes(scopes)
            .service(service)
            .authentication(authentication)
            .registeredService(registeredService)
            .grantType(getGrantType())
            .ticketGrantingTicket(ticketGrantingTicket)
            .generateRefreshToken(registeredService != null && registeredService.isGenerateRefreshToken())
            .build();
    }

    @Override
    public boolean supports(final HttpServletRequest context) {
        val grantType = context.getParameter(OAuth20Constants.GRANT_TYPE);
        return OAuth20Utils.isGrantType(grantType, getGrantType());
    }

    @Override
    public OAuth20ResponseTypes getResponseType() {
        return null;
    }

    @Override
    public OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.PASSWORD;
    }

    @Override
    public boolean requestMustBeAuthenticated() {
        return true;
    }
}
