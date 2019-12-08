package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.util.HttpRequestUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.JEEContext;
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
    protected boolean validateInternal(final JEEContext context, final String grantType,
                                       final ProfileManager manager, final UserProfile uProfile) {
        val request = context.getNativeRequest();
        val clientId = uProfile.getId();
        val redirectUri = request.getParameter(OAuth20Constants.REDIRECT_URI);
        
        LOGGER.debug("Locating registered service for client id [{}]", clientId);
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
            getConfigurationContext().getServicesManager(), clientId);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);
        
        LOGGER.debug("Received grant type [{}] with client id [{}] and redirect URI [{}]", grantType, clientId, redirectUri);
        val valid = HttpRequestUtils.doesParameterExist(request, OAuth20Constants.REDIRECT_URI)
            && HttpRequestUtils.doesParameterExist(request, OAuth20Constants.CODE)
            && OAuth20Utils.checkCallbackValid(registeredService, redirectUri);

        if (valid) {
            val code = context.getRequestParameter(OAuth20Constants.CODE)
                .map(String::valueOf).orElse(StringUtils.EMPTY);
            val token = getConfigurationContext().getTicketRegistry().getTicket(code, OAuth20Code.class);
            if (token == null || token.isExpired()) {
                LOGGER.warn("Request OAuth code [{}] is not found or has expired", code);
                return false;
            }

            val id = token.getService().getId();
            val codeRegisteredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
                getConfigurationContext().getServicesManager(), id);

            val audit = AuditableContext.builder()
                .service(token.getService())
                .authentication(token.getAuthentication())
                .registeredService(codeRegisteredService)
                .retrievePrincipalAttributesFromReleasePolicy(Boolean.TRUE)
                .build();
            val accessResult = getConfigurationContext().getRegisteredServiceAccessStrategyEnforcer().execute(audit);
            accessResult.throwExceptionIfNeeded();

            if (!registeredService.equals(codeRegisteredService)) {
                LOGGER.warn("The OAuth code [{}] issued to service [{}] does not match the registered service [{}] provided in the request given the redirect URI [{}]",
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
