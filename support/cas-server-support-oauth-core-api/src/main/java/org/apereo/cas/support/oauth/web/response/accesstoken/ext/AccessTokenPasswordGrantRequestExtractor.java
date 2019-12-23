package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.authentication.DefaultAuthenticationResult;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link AccessTokenPasswordGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class AccessTokenPasswordGrantRequestExtractor extends BaseAccessTokenGrantRequestExtractor {
    public AccessTokenPasswordGrantRequestExtractor(final OAuth20ConfigurationContext oAuthConfigurationContext) {
        super(oAuthConfigurationContext);
    }

    @Override
    public AccessTokenRequestDataHolder extract(final HttpServletRequest request, final HttpServletResponse response) {
        val context = new JEEContext(request, response, getOAuthConfigurationContext().getSessionStore());
        val clientId = OAuth20Utils.getClientIdAndClientSecret(context).getKey();
        val scopes = OAuth20Utils.parseRequestScopes(request);
        LOGGER.debug("Locating OAuth registered service by client id [{}]", clientId);

        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(getOAuthConfigurationContext().getServicesManager(), clientId);
        LOGGER.debug("Located OAuth registered service [{}]", registeredService);


        val manager = new ProfileManager<CommonProfile>(context, context.getSessionStore());
        val profile = manager.get(true);
        if (profile.isEmpty()) {
            throw new UnauthorizedServiceException("OAuth user profile cannot be determined");
        }
        val uProfile = profile.get();
        LOGGER.debug("Creating matching service request based on [{}]", registeredService);
        val requireServiceHeader = getOAuthConfigurationContext().getCasProperties().getAuthn()
            .getOauth().getGrants().getResourceOwner().isRequireServiceHeader();
        if (requireServiceHeader) {
            LOGGER.debug("Using request headers to identify and build the target service url");
        }
        val service = getOAuthConfigurationContext().getAuthenticationBuilder().buildService(registeredService, context, requireServiceHeader);

        LOGGER.debug("Authenticating the OAuth request indicated by [{}]", service);
        val authentication = getOAuthConfigurationContext().getAuthenticationBuilder().build(uProfile, registeredService, context, service);

        val audit = AuditableContext.builder()
            .service(service)
            .authentication(authentication)
            .registeredService(registeredService)
            .retrievePrincipalAttributesFromReleasePolicy(Boolean.TRUE)
            .build();
        val accessResult = getOAuthConfigurationContext().getRegisteredServiceAccessStrategyEnforcer().execute(audit);
        accessResult.throwExceptionIfNeeded();

        val result = new DefaultAuthenticationResult(authentication, requireServiceHeader ? service : null);
        val ticketGrantingTicket = getOAuthConfigurationContext().getCentralAuthenticationService().createTicketGrantingTicket(result);

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
