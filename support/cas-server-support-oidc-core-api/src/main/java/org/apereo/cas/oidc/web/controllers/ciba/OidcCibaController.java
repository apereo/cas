package org.apereo.cas.oidc.web.controllers.ciba;

import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.notifications.mail.EmailMessageBodyBuilder;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
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
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
     * Initialize backchannel verification request model and view.
     *
     * @param clientId  the client id
     * @param requestId the request id
     * @return the model and view
     * @throws Throwable the throwable
     */
    @GetMapping({
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL + "/{clientId}/{requestId}",
        "/**/" + OidcConstants.CIBA_URL + "/{clientId}/{requestId}"
    })
    public ModelAndView initializeBackchannelVerificationRequest(
        @PathVariable("clientId") final String clientId,
        @PathVariable("requestId") final String requestId) throws Throwable {
        val registeredService = findRegisteredService(clientId);
        val cibaRequest = fetchOidcCibaRequest(requestId);
        val model = new LinkedHashMap<String, Object>();
        model.put("registeredService", registeredService);
        model.put("cibaRequest", cibaRequest);
        val attributes = cibaRequest.getAuthentication().getAttributes();
        if (attributes.containsKey(OidcConstants.BINDING_MESSAGE)) {
            val bindingMessage = attributes.get(OidcConstants.BINDING_MESSAGE).getFirst();
            model.put("bindingMessage", bindingMessage);
        }
        return new ModelAndView(OidcConstants.CIBA_VERIFICATION_VIEW, model);
    }

    /**
     * Verify backchannel verification request model and view.
     *
     * @param clientId  the client id
     * @param requestId the request id
     * @return the model and view
     * @throws Throwable the throwable
     */
    @PostMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL + "/{clientId}/{requestId}",
        "/**/" + OidcConstants.CIBA_URL + "/{clientId}/{requestId}"
    }, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity verifyBackchannelVerificationRequest(
        @PathVariable("clientId") final String clientId,
        @PathVariable("requestId") final String requestId) throws Throwable {
        val registeredService = findRegisteredService(clientId);
        val cibaRequest = fetchOidcCibaRequest(requestId);
        val model = new LinkedHashMap<String, Object>();
        model.put("registeredService", registeredService);
        model.put("cibaRequest", cibaRequest);
        return ResponseEntity.ok(model);
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
        val webContext = new JEEContext(request, response);
        val cibaRequest = buildCibaRequestContext(webContext);

        var hintCount = StringUtils.isNotBlank(cibaRequest.getLoginHint()) ? 1 : 0;
        hintCount += StringUtils.isNotBlank(cibaRequest.getLoginHintToken()) ? 1 : 0;
        hintCount += StringUtils.isNotBlank(cibaRequest.getIdTokenHint()) ? 1 : 0;
        if (hintCount > 1) {
            return badCibaRequest("More than one hint specified");
        }
        if (cibaRequest.getScope().isEmpty() || !cibaRequest.getScope().contains(OidcConstants.StandardScopes.OPENID.getScope())) {
            return badCibaRequest("Scope must contain %s".formatted(OidcConstants.StandardScopes.OPENID.getScope()));
        }
        val registeredService = findRegisteredService(cibaRequest);
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
        LOGGER.debug("Resolved CIBA principal to be [{}]", principal);
        val emailProperties = configurationContext.getCasProperties().getAuthn().getOidc().getCiba().getVerification().getMail();
        if (!CollectionUtils.containsAny(principal.getAttributes().keySet(), emailProperties.getAttributeName())) {
            return badCibaRequest("Principal does not contain required attributes for notification");
        }
        val cibaRequestId = recordCibaRequest(cibaRequest, principal);
        val cibaResponse = buildCibaResponse(cibaRequestId);
        scheduleUserVerificationRequest(cibaRequestId, cibaRequest, registeredService);
        return cibaResponse;
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    protected void scheduleUserVerificationRequest(final OidcCibaRequest cibaRequestId,
                                                   final CibaRequestContext cibaRequest,
                                                   final OidcRegisteredService registeredService) {
        val verification = configurationContext.getCasProperties().getAuthn().getOidc().getCiba().getVerification();
        val delayInSeconds = Beans.newDuration(verification.getDelay()).toSeconds();
        configurationContext.getTaskScheduler().schedule(
            () -> notifyUserForCibaRequestVerification(cibaRequestId, cibaRequest, registeredService),
            LocalDateTime.now(Clock.systemUTC()).plusSeconds(delayInSeconds).toInstant(ZoneOffset.UTC));
    }

    protected void notifyUserForCibaRequestVerification(final OidcCibaRequest cibaRequest,
                                                        final CibaRequestContext cibaRequestContext,
                                                        final OidcRegisteredService registeredService) {
        val mail = configurationContext.getCasProperties().getAuthn().getOidc().getCiba().getVerification().getMail();
        val principal = cibaRequest.getAuthentication().getPrincipal();
        mail.getAttributeName().forEach(attributeName -> {
            val resolvedAttribute = SpringExpressionLanguageValueResolver.getInstance().resolve(attributeName);
            if (principal.getAttributes().containsKey(resolvedAttribute)) {
                val verificationUrl = buildCibaVerificationUrl(cibaRequest);
                val addresses = (List) principal.getAttributes().get(resolvedAttribute);
                val parameters = Map.<String, Object>of(
                    "verificationUrl", verificationUrl,
                    "registeredService", registeredService,
                    "cibaRequest", cibaRequest);
                val body = EmailMessageBodyBuilder.builder()
                    .properties(mail)
                    .parameters(parameters)
                    .build()
                    .get();
                val emailRequest = EmailMessageRequest.builder()
                    .emailProperties(mail)
                    .to(addresses)
                    .body(body)
                    .build();
                addresses.forEach(address -> configurationContext.getCommunicationsManager().email(emailRequest));
            } else {
                LOGGER.warn("Could not send email to [{}]. No email found for [{}] or email settings are not configured.", principal.getId(), resolvedAttribute);
            }
        });
    }

    private String buildCibaVerificationUrl(final OidcCibaRequest cibaRequest) {
        return FunctionUtils.doUnchecked(() ->
            new URIBuilder(configurationContext.getCasProperties().getServer().getPrefix())
                .appendPath(OidcConstants.BASE_OIDC_URL)
                .appendPath(OidcConstants.CIBA_URL)
                .appendPath(cibaRequest.getClientId())
                .appendPath(cibaRequest.getId())
                .build()
                .toString());
    }

    protected OidcRegisteredService findRegisteredService(final CibaRequestContext cibaRequest) {
        return findRegisteredService(cibaRequest.getClientId());
    }

    protected OidcRegisteredService findRegisteredService(final String clientId) {
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(configurationContext.getServicesManager(), clientId);
        RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);
        return (OidcRegisteredService) registeredService;
    }

    protected CibaRequestContext buildCibaRequestContext(final WebContext webContext) {
        val manager = new ProfileManager(webContext, getConfigurationContext().getSessionStore());
        val requestParameterResolver = getConfigurationContext().getRequestParameterResolver();
        return CibaRequestContext.builder()
            .acrValues(requestParameterResolver.resolveRequestParameters(webContext, OidcConstants.ACR_VALUES))
            .bindingMessage(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.BINDING_MESSAGE).orElse(null))
            .clientNotificationToken(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.CLIENT_NOTIFICATION_TOKEN).orElse(null))
            .idTokenHint(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.ID_TOKEN_HINT).orElse(null))
            .loginHint(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.LOGIN_HINT).orElse(null))
            .loginHintToken(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.LOGIN_HINT_TOKEN).orElse(null))
            .requestedExpiry(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.REQUESTED_EXPIRY, Long.class).orElse(0L))
            .scope(requestParameterResolver.resolveRequestScopes(webContext))
            .clientId(manager.getProfile().orElseThrow().getAttribute(OAuth20Constants.CLIENT_ID).toString())
            .userCode(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.USER_CODE).orElse(null))
            .build();
    }

    protected ResponseEntity buildCibaResponse(final OidcCibaRequest cibaRequestId) {
        val encodedId = (byte[]) configurationContext.getWebflowCipherExecutor()
            .withSigningDisabled()
            .encode(cibaRequestId.getId().getBytes(StandardCharsets.UTF_8));
        return ResponseEntity.ok(Map.of(
            OidcConstants.AUTH_REQ_ID, Objects.requireNonNull(EncodingUtils.encodeUrlSafeBase64(encodedId)),
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

    private OidcCibaRequest fetchOidcCibaRequest(final String requestId) {
        val decoded = EncodingUtils.decodeUrlSafeBase64(requestId);
        val decodedId = new String((byte[]) configurationContext.getWebflowCipherExecutor().withSigningDisabled().decode(decoded), StandardCharsets.UTF_8);
        val cibaRequest = configurationContext.getTicketRegistry().getTicket(decodedId, OidcCibaRequest.class);
        Assert.notNull(cibaRequest, "CIBA request cannot be found");
        return cibaRequest;
    }
}
