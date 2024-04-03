package org.apereo.cas.oidc.web.controllers.ciba;

import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.web.controllers.BaseOidcController;
import org.apereo.cas.services.OidcBackchannelTokenDeliveryModes;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Map;

/**
 * This is {@link OidcCibaController}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Slf4j
public class OidcCibaController extends BaseOidcController {
    
    public OidcCibaController(final OidcConfigurationContext configurationContext) {
        super(configurationContext);
    }

    /**
     * Handle back-channel authn request.
     *
     * @param request  the request
     * @param response the response
     * @return the response entity
     */
    @PostMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL,
        "/**/" + OidcConstants.CIBA_URL}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity handleBackchannelAuthnRequest(final HttpServletRequest request, final HttpServletResponse response) {
        val context = new JEEContext(request, response);
        val manager = new ProfileManager(context, getConfigurationContext().getSessionStore());
        val requestParameterResolver = getConfigurationContext().getRequestParameterResolver();
        val webContext = new JEEContext(request, response);
        val cibaRequest = CibaRequest.builder()
            .acrValues(requestParameterResolver.resolveRequestParameters(webContext, OidcConstants.ACR_VALUES))
            .bindingMessage(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.BINDING_MESSAGE).orElse(null))
            .clientNotificationToken(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.CLIENT_NOTIFICATION_TOKEN).orElse(null))
            .idTokenHint(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.ID_TOKEN_HINT).orElse(null))
            .loginHint(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.LOGIN_HINT).orElse(null))
            .loginHintToken(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.LOGIN_HINT_TOKEN).orElse(null))
            .requestedExpiry(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.REQUESTED_EXPIRY, Long.class).orElse(0L))
            .scope(requestParameterResolver.resolveRequestScopes(context))
            .userCode(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.USER_CODE).orElse(null))
            .build();

        var hintCount = StringUtils.isNotBlank(cibaRequest.getLoginHint()) ? 1 : 0;
        hintCount += StringUtils.isNotBlank(cibaRequest.getLoginHintToken()) ? 1 : 0;
        hintCount += StringUtils.isNotBlank(cibaRequest.getIdTokenHint()) ? 1 : 0;
        if (hintCount > 1) {
            return ResponseEntity.badRequest().body(OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, "More than one hint specified"));
        }
        if (cibaRequest.getScope().isEmpty() || !cibaRequest.getScope().contains(OidcConstants.StandardScopes.OPENID.getScope())) {
            return ResponseEntity.badRequest().body(OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST,
                "Scope must contain %s".formatted(OidcConstants.StandardScopes.OPENID.getScope())));
        }
        val clientId = manager.getProfile().orElseThrow().getAttribute(OAuth20Constants.CLIENT_ID).toString();
        val registeredService = (OidcRegisteredService) OAuth20Utils.getRegisteredOAuthServiceByClientId(configurationContext.getServicesManager(), clientId);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);
        if (!registeredService.isBackchannelUserCodeParameterSupported() && !configurationContext.getDiscoverySettings().isBackchannelUserCodeParameterSupported()
            && StringUtils.isNotBlank(cibaRequest.getUserCode())) {
            return ResponseEntity.badRequest().body(OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, "User code is not supported"));
        }
        if ((registeredService.isBackchannelUserCodeParameterSupported() || configurationContext.getDiscoverySettings().isBackchannelUserCodeParameterSupported())
            && StringUtils.isBlank(cibaRequest.getUserCode())) {
            return ResponseEntity.badRequest().body(OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, "User code is required"));
        }
        val deliveryMode = OidcBackchannelTokenDeliveryModes.valueOf(registeredService.getBackchannelTokenDeliveryMode().toUpperCase(Locale.ENGLISH));
        if ((deliveryMode == OidcBackchannelTokenDeliveryModes.PUSH || deliveryMode == OidcBackchannelTokenDeliveryModes.PING)
            && StringUtils.isBlank(cibaRequest.getClientNotificationToken())) {
            return ResponseEntity.badRequest().body(OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, "Client notification token is required"));
        }

        return ResponseEntity.ok(Map.of());
    }
}
