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
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;

import java.util.Objects;

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
    public AccessTokenRequestContext extractRequest(final WebContext context) throws Throwable {
        val callContext = new CallContext(context, getConfigurationContext().getSessionStore());
        val clientId = getConfigurationContext().getRequestParameterResolver()
            .resolveClientIdAndClientSecret(callContext).getKey();

        LOGGER.debug("Locating OAuth registered service by client id [{}]", clientId);
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(getConfigurationContext().getServicesManager(), clientId);
        LOGGER.debug("Located OAuth registered service [{}]", registeredService);

        val manager = new ProfileManager(context, getConfigurationContext().getSessionStore());
        val profile = manager.getProfile().orElseThrow(() -> UnauthorizedServiceException.denied("OAuth user profile cannot be determined"));
        LOGGER.debug("Creating matching service request based on [{}]", registeredService);
        val requireServiceHeader = getConfigurationContext().getCasProperties().getAuthn()
            .getOauth().getGrants().getResourceOwner().isRequireServiceHeader();
        val service = getConfigurationContext().getAuthenticationBuilder().buildService(registeredService, context, requireServiceHeader);

        LOGGER.debug("Authenticating the OAuth request indicated by [{}]", service);
        val authentication = getConfigurationContext().getAuthenticationBuilder().build(profile, registeredService, context, service);

        val audit = AuditableContext.builder()
            .service(service)
            .authentication(authentication)
            .registeredService(registeredService)
            .build();
        val accessResult = getConfigurationContext().getRegisteredServiceAccessStrategyEnforcer().execute(audit);
        accessResult.throwExceptionIfNeeded();

        val result = new DefaultAuthenticationResult(authentication, requireServiceHeader ? service : null);
        val ticketGrantingTicket = getConfigurationContext().getCentralAuthenticationService().createTicketGrantingTicket(result);

        val scopes = getConfigurationContext().getRequestParameterResolver().resolveRequestScopes(context);
        scopes.retainAll(Objects.requireNonNull(registeredService).getScopes());

        return AccessTokenRequestContext.builder()
            .scopes(scopes)
            .service(service)
            .authentication(authentication)
            .registeredService(registeredService)
            .grantType(getGrantType())
            .ticketGrantingTicket(ticketGrantingTicket)
            .generateRefreshToken(registeredService.isGenerateRefreshToken())
            .build();
    }

    @Override
    public boolean supports(final WebContext context) {
        val grantType = getConfigurationContext().getRequestParameterResolver()
            .resolveRequestParameter(context, OAuth20Constants.GRANT_TYPE).orElse(StringUtils.EMPTY);
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
