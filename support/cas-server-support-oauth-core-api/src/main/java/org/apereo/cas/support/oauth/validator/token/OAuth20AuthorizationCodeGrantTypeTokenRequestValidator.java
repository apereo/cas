package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.code.OAuth20Code;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;

/**
 * This is {@link OAuth20AuthorizationCodeGrantTypeTokenRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class OAuth20AuthorizationCodeGrantTypeTokenRequestValidator extends BaseOAuth20TokenRequestValidator {
    public OAuth20AuthorizationCodeGrantTypeTokenRequestValidator(final OAuth20ConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    protected OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.AUTHORIZATION_CODE;
    }

    @Override
    protected boolean validateInternal(final WebContext context, final String grantType,
                                       final ProfileManager manager, final UserProfile uProfile) {
        val clientId = uProfile.getId();
        val redirectUri = OAuth20Utils.getRequestParameter(context, OAuth20Constants.REDIRECT_URI);
        val code = OAuth20Utils.getRequestParameter(context, OAuth20Constants.CODE);

        LOGGER.debug("Locating registered service for client id [{}]", clientId);
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
            getConfigurationContext().getServicesManager(), clientId);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);

        LOGGER.debug("Received grant type [{}] with client id [{}] and redirect URI [{}]", grantType, clientId, redirectUri);
        val valid = redirectUri.isPresent() && code.isPresent() && OAuth20Utils.checkCallbackValid(registeredService, redirectUri.get());

        if (valid) {
            val token = getConfigurationContext().getTicketRegistry().getTicket(code.get(), OAuth20Code.class);
            if (token == null || token.isExpired()) {
                LOGGER.debug("Code [{}] is invalid or expired. Attempting to revoke access tokens issued to the code", code.get());
                val accessTokensByCode = getConfigurationContext().getTicketRegistry().getTickets(ticket ->
                    ticket instanceof OAuth20AccessToken
                        && StringUtils.equalsIgnoreCase(((OAuth20AccessToken) ticket).getToken(), code.get()));
                accessTokensByCode.forEach(ticket -> {
                    LOGGER.debug("Removing access token [{}] issued via expired/unknown code [{}]", ticket.getId(), code.get());
                    getConfigurationContext().getTicketRegistry().deleteTicket(ticket);
                });

                LOGGER.warn("Request OAuth code [{}] is not found or has expired", code.get());
                return false;
            }

            val id = token.getService().getId();
            val codeRegisteredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
                getConfigurationContext().getServicesManager(), id);

            val audit = AuditableContext.builder()
                .service(token.getService())
                .authentication(token.getAuthentication())
                .registeredService(codeRegisteredService)
                .build();
            val accessResult = getConfigurationContext().getRegisteredServiceAccessStrategyEnforcer().execute(audit);
            accessResult.throwExceptionIfNeeded();

            if (!registeredService.equals(codeRegisteredService)) {
                LOGGER.warn("OAuth code [{}] issued to service [{}] does not match [{}] provided, given the redirect URI [{}]",
                    code, id, registeredService.getName(), redirectUri);
                return false;
            }

            if (!isGrantTypeSupportedBy(registeredService, grantType)) {
                LOGGER.warn("Requested grant type [{}] is not authorized by service definition [{}]", getGrantType(), registeredService.getServiceId());
                return false;
            }

            return true;
        }
        LOGGER.warn("Access token request cannot be validated for grant type [{}] and client id [{}] given the redirect URI [{}]", grantType, clientId, redirectUri);
        return false;
    }
}
