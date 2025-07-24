package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Strings;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link OAuth20AuthorizationCodeGrantTypeTokenRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class OAuth20AuthorizationCodeGrantTypeTokenRequestValidator extends BaseOAuth20TokenRequestValidator<OAuth20ConfigurationContext> {
    public OAuth20AuthorizationCodeGrantTypeTokenRequestValidator(final ObjectProvider<OAuth20ConfigurationContext> configurationContext) {
        super(configurationContext);
    }

    @Override
    protected OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.AUTHORIZATION_CODE;
    }

    @Override
    protected boolean validateInternal(final WebContext context, final String grantType,
                                       final ProfileManager manager, final UserProfile uProfile) throws Throwable {
        val configurationContext = getConfigurationContext().getObject();
        val redirectUri = configurationContext.getRequestParameterResolver()
            .resolveRequestParameter(context, OAuth20Constants.REDIRECT_URI);
        
        val code = configurationContext.getRequestParameterResolver()
            .resolveRequestParameter(context, OAuth20Constants.CODE);

        val clientId = ObjectUtils.defaultIfNull(uProfile.getAttribute(OAuth20Constants.CLIENT_ID), uProfile.getId()).toString();
        LOGGER.debug("Locating registered service for client id [{}]", clientId);
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
            configurationContext.getServicesManager(), clientId);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);

        LOGGER.debug("Received grant type [{}] with client id [{}] and redirect URI [{}]", grantType, clientId, redirectUri);
        val valid = redirectUri.isPresent() && code.isPresent() && OAuth20Utils.checkCallbackValid(registeredService, redirectUri.get());

        if (valid) {
            val oauthCode = FunctionUtils.doAndHandle(() -> {
                val state = configurationContext.getTicketRegistry().getTicket(code.get(), OAuth20Code.class);
                return state == null || state.isExpired() ? null : state;
            });
            if (oauthCode == null || oauthCode.isExpired()) {
                val removeTokens = configurationContext.getCasProperties().getAuthn().getOauth().getCode().isRemoveRelatedAccessTokens();
                if (removeTokens) {
                    LOGGER.debug("Code [{}] is invalid or expired. Attempting to revoke access tokens issued to the code", code.get());
                    val accessTokensByCode = configurationContext.getTicketRegistry().getTickets(ticket ->
                        ticket instanceof final OAuth20AccessToken accessToken
                        && Strings.CI.equals(accessToken.getToken(), code.get()));
                    accessTokensByCode.forEach(Unchecked.consumer(ticket -> {
                        LOGGER.debug("Removing access token [{}] issued via expired/unknown code [{}]", ticket.getId(), code.get());
                        configurationContext.getTicketRegistry().deleteTicket(ticket);
                    }));
                }
                LOGGER.warn("Provided OAuth code [{}] is not found or has expired", code.get());
                return false;
            }

            val serviceId = oauthCode.getService().getId();
            val codeRegisteredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
                configurationContext.getServicesManager(), serviceId);

            val authentication = resolveAuthenticationFrom(oauthCode);
            val originalPrincipal = authentication.getPrincipal();
            val accessStrategyAttributes = CoreAuthenticationUtils.mergeAttributes(originalPrincipal.getAttributes(),
                oauthCode.getAuthentication().getPrincipal().getAttributes());
            val accessStrategyPrincipal = configurationContext.getPrincipalFactory()
                .createPrincipal(oauthCode.getAuthentication().getPrincipal().getId(), accessStrategyAttributes);
            val audit = AuditableContext.builder()
                .service(oauthCode.getService())
                .registeredService(codeRegisteredService)
                .authentication(oauthCode.getAuthentication())
                .principal(accessStrategyPrincipal)
                .build();
            val accessResult = configurationContext.getRegisteredServiceAccessStrategyEnforcer().execute(audit);
            accessResult.throwExceptionIfNeeded();

            if (!registeredService.equals(codeRegisteredService)) {
                LOGGER.warn("OAuth code [{}] issued to service [{}] does not match [{}] provided, given the redirect URI [{}]",
                    code, serviceId, registeredService.getName(), redirectUri);
                return false;
            }

            if (!isGrantTypeSupportedBy(registeredService, grantType)) {
                LOGGER.warn("Requested grant type [{}] is not authorized by service definition [{}]", grantType, registeredService.getServiceId());
                return false;
            }
            return true;
        }
        LOGGER.warn("Access token request cannot be validated for grant type [{}] and client id [{}] given the redirect URI [{}]", grantType, clientId, redirectUri);
        return false;
    }
}
