package org.apereo.cas.support.oauth.validator.token;

import module java.base;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.ticket.code.OAuth20Code;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.ObjectProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
    protected boolean validateInternal(final HttpServletRequest request, final HttpServletResponse response, final String grantType,
                                       final OAuth20UserProfile uProfile)
            throws Throwable {
        val configurationContext = getConfigurationContext().getObject();
        val redirectUri = configurationContext.getRequestParameterResolver()
            .resolveRequestParameter(request, OAuth20Constants.REDIRECT_URI);
        
        val code = configurationContext.getRequestParameterResolver()
            .resolveRequestParameter(request, OAuth20Constants.CODE);

        val clientId = ObjectUtils.getIfNull(uProfile.getAttribute(OAuth20Constants.CLIENT_ID), uProfile.getId()).toString();
        LOGGER.debug("Locating registered service for client id [{}]", clientId);
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
            configurationContext.getServicesManager(), clientId);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);

        LOGGER.debug("Received grant type [{}] with client id [{}] and redirect URI [{}]", grantType, clientId, redirectUri);
        val valid = redirectUri.isPresent() && code.isPresent() && OAuth20Utils.checkCallbackValid(registeredService, redirectUri.get());

        if (valid) {
            val oauthCode = OAuth20Utils.getRequestedOAuthToken(request, configurationContext.getTicketRegistry(), code.get(), OAuth20Code.class);
            if (oauthCode == null || oauthCode.isExpired()) {
                OAuth20Utils.revokeTokensIssuedFromCode(configurationContext.getTicketRegistry(), code.get());
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
            val codeVerifier = configurationContext.getRequestParameterResolver()
                .resolveRequestParameter(request, OAuth20Constants.CODE_VERIFIER).orElse(null);
            if (!OAuth20Utils.isCodeVerifierValidFor(oauthCode, codeVerifier)) {
                LOGGER.warn("Code verifier does not match the code challenge of OAuth code [{}], or no challenge was issued with the code", code.get());
                return false;
            }
            verifyBoundProofOfPossessionKey(oauthCode, OAuth20Constants.DPOP_JKT, uProfile);
            return true;
        }
        LOGGER.warn("Access token request cannot be validated for grant type [{}] and client id [{}] given the redirect URI [{}]", grantType, clientId, redirectUri);
        return false;
    }
}
