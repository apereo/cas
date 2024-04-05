package org.apereo.cas.oidc.web.controllers.ciba;

import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.ticket.OidcCibaRequest;
import org.apereo.cas.oidc.ticket.OidcCibaRequestFactory;
import org.apereo.cas.oidc.web.controllers.BaseOidcController;
import org.apereo.cas.services.OidcBackchannelTokenDeliveryModes;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.token.JwtBuilder;
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
import java.util.Optional;

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
    public ResponseEntity handleBackchannelAuthnRequest(final HttpServletRequest request, final HttpServletResponse response) throws Throwable {
        val context = new JEEContext(request, response);
        val manager = new ProfileManager(context, getConfigurationContext().getSessionStore());
        val requestParameterResolver = getConfigurationContext().getRequestParameterResolver();
        val webContext = new JEEContext(request, response);
        val cibaRequest = CibaRequestContext.builder()
            .acrValues(requestParameterResolver.resolveRequestParameters(webContext, OidcConstants.ACR_VALUES))
            .bindingMessage(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.BINDING_MESSAGE).orElse(null))
            .clientNotificationToken(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.CLIENT_NOTIFICATION_TOKEN).orElse(null))
            .idTokenHint(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.ID_TOKEN_HINT).orElse(null))
            .loginHint(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.LOGIN_HINT).orElse(null))
            .loginHintToken(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.LOGIN_HINT_TOKEN).orElse(null))
            .requestedExpiry(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.REQUESTED_EXPIRY, Long.class).orElse(0L))
            .scope(requestParameterResolver.resolveRequestScopes(context))
            .clientId(manager.getProfile().orElseThrow().getAttribute(OAuth20Constants.CLIENT_ID).toString())
            .userCode(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.USER_CODE).orElse(null))
            .build();

        var hintCount = StringUtils.isNotBlank(cibaRequest.getLoginHint()) ? 1 : 0;
        hintCount += StringUtils.isNotBlank(cibaRequest.getLoginHintToken()) ? 1 : 0;
        hintCount += StringUtils.isNotBlank(cibaRequest.getIdTokenHint()) ? 1 : 0;
        if (hintCount > 1) {
            return badCibaRequest("More than one hint specified");
        }
        if (cibaRequest.getScope().isEmpty() || !cibaRequest.getScope().contains(OidcConstants.StandardScopes.OPENID.getScope())) {
            return badCibaRequest("Scope must contain %s".formatted(OidcConstants.StandardScopes.OPENID.getScope()));
        }
        val registeredService = (OidcRegisteredService) OAuth20Utils.getRegisteredOAuthServiceByClientId(configurationContext.getServicesManager(), cibaRequest.getClientId());
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);
        if (!registeredService.isBackchannelUserCodeParameterSupported() && !configurationContext.getDiscoverySettings().isBackchannelUserCodeParameterSupported()
            && StringUtils.isNotBlank(cibaRequest.getUserCode())) {
            return badCibaRequest("User code is not supported");
        }
        if ((registeredService.isBackchannelUserCodeParameterSupported() || configurationContext.getDiscoverySettings().isBackchannelUserCodeParameterSupported())
            && StringUtils.isBlank(cibaRequest.getUserCode())) {
            return badCibaRequest("User code is required");
        }
        val deliveryMode = OidcBackchannelTokenDeliveryModes.valueOf(registeredService.getBackchannelTokenDeliveryMode().toUpperCase(Locale.ENGLISH));
        if ((deliveryMode == OidcBackchannelTokenDeliveryModes.PUSH || deliveryMode == OidcBackchannelTokenDeliveryModes.PING)
            && StringUtils.isBlank(cibaRequest.getClientNotificationToken())) {
            return badCibaRequest("Client notification token is required");
        }
        val principal = determineCibaRequestPrincipal(cibaRequest, registeredService);
        if (principal == null) {
            return badCibaRequest("Unable to determine subject");
        }

        val cibaRequestId = recordCibaRequest(cibaRequest, principal);
        return buildCibaResponse(cibaRequestId);
    }

    protected ResponseEntity buildCibaResponse(final OidcCibaRequest cibaRequestId) {
        return ResponseEntity.ok(Map.of(
            OidcConstants.AUTH_REQ_ID, cibaRequestId.getId(),
            OAuth20Constants.EXPIRES_IN, cibaRequestId.getExpirationPolicy().getTimeToLive()
        ));
    }

    protected OidcCibaRequest recordCibaRequest(final CibaRequestContext cibaRequest, final Principal principal) throws Throwable {
        val cibaFactory = (OidcCibaRequestFactory) configurationContext.getTicketFactory().get(OidcCibaRequest.class);
        val cibaRequestId = cibaFactory.create(cibaRequest.withPrincipal(principal));
        configurationContext.getTicketRegistry().addTicket(cibaRequestId);
        return cibaRequestId;
    }

    protected ResponseEntity badCibaRequest(final String error) {
        val body = OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, error);
        return ResponseEntity.badRequest().body(body);
    }

    protected Principal determineCibaRequestPrincipal(final CibaRequestContext cibaRequest,
                                                      final OidcRegisteredService registeredService) throws Throwable {
        var subject = cibaRequest.getLoginHint();
        if (StringUtils.isNotBlank(cibaRequest.getIdTokenHint())) {
            val claims = configurationContext.getIdTokenSigningAndEncryptionService()
                .decode(cibaRequest.getIdTokenHint(), Optional.of(registeredService));
            subject = claims.getSubject();
        }
        if (StringUtils.isNotBlank(cibaRequest.getLoginHintToken())) {
            subject = JwtBuilder.parse(cibaRequest.getLoginHint()).getSubject();
        }
        if (StringUtils.isNotBlank(subject)) {
            return configurationContext.getPrincipalResolver().resolve(new BasicIdentifiableCredential(subject));
        }
        return null;
    }
}
