package org.apereo.cas.oidc.web.controllers.ciba;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.multitenancy.TenantDefinition;
import org.apereo.cas.notifications.mail.EmailCommunicationResult;
import org.apereo.cas.notifications.mail.EmailMessageBodyBuilder;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.ticket.OidcCibaRequest;
import org.apereo.cas.oidc.ticket.OidcCibaRequestFactory;
import org.apereo.cas.oidc.token.ciba.CibaTokenDeliveryHandler;
import org.apereo.cas.oidc.web.controllers.BaseOidcController;
import org.apereo.cas.services.OidcBackchannelTokenDeliveryModes;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.apereo.inspektr.audit.annotation.Audit;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
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
@Tag(name = "OpenID Connect")
public class OidcCibaController extends BaseOidcController {
    protected final List<CibaTokenDeliveryHandler> tokenDeliveryHandlers;

    public OidcCibaController(final OidcConfigurationContext configurationContext,
                              final List<CibaTokenDeliveryHandler> tokenDeliveryHandlers) {
        super(configurationContext);
        this.tokenDeliveryHandlers = List.copyOf(tokenDeliveryHandlers);
    }

    /**
     * Initialize backchannel verification request model and view.
     *
     * @param clientId  the client id
     * @param requestId the request id
     * @return the model and view
     */
    @GetMapping({
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL + "/{clientId}/{requestId}",
        "/**/" + OidcConstants.CIBA_URL + "/{clientId}/{requestId}"
    })
    @Operation(summary = "Initialize backchannel verification request",
        parameters = {
            @Parameter(name = "clientId", in = ParameterIn.PATH, description = "Client ID"),
            @Parameter(name = "requestId", in = ParameterIn.PATH, description = "Request ID")
        })
    public Object initializeBackchannelVerificationRequest(
        @PathVariable("clientId")
        final String clientId,
        @PathVariable("requestId")
        final String requestId) {
        try {
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
            model.put("userCodeRequired", cibaRequest.getAuthentication().containsAttribute(OidcConstants.USER_CODE));
            return new ModelAndView(OidcConstants.CIBA_VERIFICATION_VIEW, model);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
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
    @Operation(summary = "Verify backchannel verification request",
        parameters = {
            @Parameter(name = "clientId", in = ParameterIn.PATH, description = "Client ID"),
            @Parameter(name = "requestId", in = ParameterIn.PATH, description = "Request ID"),
            @Parameter(name = "userCode", in = ParameterIn.QUERY, required = false, description = "Request ID")
        })
    public ResponseEntity verifyBackchannelVerificationRequest(
        @RequestParam(value = "userCode", required = false)
        final String userCode,
        @PathVariable("clientId")
        final String clientId,
        @PathVariable("requestId")
        final String requestId) throws Throwable {
        try {
            val registeredService = findRegisteredService(clientId);
            val cibaRequest = fetchOidcCibaRequest(requestId);
            if (cibaRequest.getAuthentication().containsAttribute(OidcConstants.USER_CODE)) {
                val userCodeValues = cibaRequest.getAuthentication().getAttributes().get(OidcConstants.USER_CODE)
                    .stream().map(Object::toString).filter(StringUtils::isNotBlank).toList();
                if (StringUtils.isBlank(userCode) || !userCodeValues.contains(userCode)) {
                    throw new AuthenticationException("Unable to verify provided user code " + userCode);
                }
            }

            for (val handler : tokenDeliveryHandlers) {
                if (BeanSupplier.isNotProxy(handler) && handler.supports(registeredService)) {
                    handler.deliver(registeredService, cibaRequest);
                }
            }

            val model = new LinkedHashMap<String, Object>();
            model.put("registeredService", registeredService);
            model.put("cibaRequest", cibaRequest);
            return ResponseEntity.ok(model);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
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
    @Audit(action = AuditableActions.OIDC_CIBA_RESPONSE,
        actionResolverName = AuditActionResolvers.OIDC_CIBA_RESPONSE_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.OIDC_CIBA_RESPONSE_RESOURCE_RESOLVER)
    @Operation(summary = "Handle back-channel authn request")
    public ResponseEntity handleBackchannelAuthnRequest(final HttpServletRequest request, final HttpServletResponse response) throws Throwable {
        val webContext = new JEEContext(request, response);
        val cibaRequestContext = buildCibaRequestContext(webContext);

        var hintCount = StringUtils.isNotBlank(cibaRequestContext.getLoginHint()) ? 1 : 0;
        hintCount += StringUtils.isNotBlank(cibaRequestContext.getLoginHintToken()) ? 1 : 0;
        hintCount += StringUtils.isNotBlank(cibaRequestContext.getIdTokenHint()) ? 1 : 0;
        if (hintCount > 1) {
            return badCibaRequest("More than one hint specified");
        }
        if (cibaRequestContext.getScope().isEmpty() || !cibaRequestContext.getScope().contains(OidcConstants.StandardScopes.OPENID.getScope())) {
            return badCibaRequest("Scope must contain %s".formatted(OidcConstants.StandardScopes.OPENID.getScope()));
        }
        val registeredService = findRegisteredService(cibaRequestContext);
        if (!registeredService.isBackchannelUserCodeParameterSupported() && !configurationContext.getDiscoverySettings().isBackchannelUserCodeParameterSupported()
            && StringUtils.isNotBlank(cibaRequestContext.getUserCode())) {
            return badCibaRequest("User code is not supported");
        }
        val deliveryMode = OidcBackchannelTokenDeliveryModes.valueOf(registeredService.getBackchannelTokenDeliveryMode().toUpperCase(Locale.ENGLISH));
        if ((deliveryMode == OidcBackchannelTokenDeliveryModes.PUSH || deliveryMode == OidcBackchannelTokenDeliveryModes.PING)
            && StringUtils.isBlank(cibaRequestContext.getClientNotificationToken())) {
            return badCibaRequest("Client notification token is required");
        }
        if (StringUtils.isBlank(registeredService.getBackchannelClientNotificationEndpoint())
            || !StringUtils.startsWithIgnoreCase(registeredService.getBackchannelClientNotificationEndpoint(), "https://")) {
            return badCibaRequest("Client backchannel notification endpoint is invalid");
        }
        val principal = determineCibaRequestPrincipal(cibaRequestContext, registeredService);
        if (principal == null) {
            return badCibaRequest("Unable to determine subject");
        }
        LOGGER.debug("Resolved CIBA principal to be [{}]", principal);
        val emailProperties = configurationContext.getCasProperties().getAuthn().getOidc().getCiba().getVerification().getMail();
        if (!CollectionUtils.containsAny(principal.getAttributes().keySet(), emailProperties.getAttributeName())) {
            return badCibaRequest("Principal does not contain required attributes for notification");
        }
        val cibaRequest = recordCibaRequest(cibaRequestContext.withPrincipal(principal));
        request.setAttribute(CibaRequestContext.class.getName(), cibaRequestContext);
        request.setAttribute(Principal.class.getName(), principal);
        val cibaResponse = buildCibaResponse(cibaRequest);
        scheduleUserVerificationRequest(cibaRequest, cibaRequestContext, registeredService);
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
        val verificationUrl = buildCibaVerificationUrl(cibaRequest);
        val parameters = Map.<String, Object>of(
            "verificationUrl", verificationUrl,
            "registeredService", registeredService,
            "cibaRequest", cibaRequest);
        val body = EmailMessageBodyBuilder.builder()
            .properties(mail)
            .parameters(parameters)
            .build()
            .get();
        val sent = mail.getAttributeName()
            .stream()
            .map(attributeName -> {
                val resolvedAttribute = SpringExpressionLanguageValueResolver.getInstance().resolve(attributeName);
                val emailRequest = EmailMessageRequest.builder()
                    .emailProperties(mail)
                    .body(body)
                    .principal(principal)
                    .attribute(resolvedAttribute)
                    .tenant(cibaRequestContext.getTenant())
                    .build();
                return configurationContext.getCommunicationsManager().email(emailRequest);
            })
            .anyMatch(EmailCommunicationResult::isSuccess);
        Assert.isTrue(sent, "Could not send email to " + principal.getId());
    }

    private String buildCibaVerificationUrl(final OidcCibaRequest cibaRequest) {
        return FunctionUtils.doUnchecked(() -> new URIBuilder(configurationContext.getCasProperties().getServer().getPrefix())
            .appendPath(OidcConstants.BASE_OIDC_URL)
            .appendPath(OidcConstants.CIBA_URL)
            .appendPath(cibaRequest.getClientId())
            .appendPath(cibaRequest.getEncodedId())
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
        val userProfile = manager.getProfile().orElseThrow();
        val clientId = userProfile.getAttribute(OAuth20Constants.CLIENT_ID).toString();

        val tenant = getConfigurationContext().getTenantExtractor().extract(webContext.getRequestURL())
            .map(TenantDefinition::getId).orElse(StringUtils.EMPTY);

        return CibaRequestContext.builder()
            .acrValues(requestParameterResolver.resolveRequestParameters(webContext, OidcConstants.ACR_VALUES))
            .bindingMessage(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.BINDING_MESSAGE).orElse(null))
            .clientNotificationToken(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.CLIENT_NOTIFICATION_TOKEN).orElse(null))
            .idTokenHint(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.ID_TOKEN_HINT).orElse(null))
            .loginHint(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.LOGIN_HINT).orElse(null))
            .loginHintToken(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.LOGIN_HINT_TOKEN).orElse(null))
            .requestedExpiry(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.REQUESTED_EXPIRY, Long.class).orElse(0L))
            .scope(requestParameterResolver.resolveRequestScopes(webContext))
            .clientId(clientId)
            .userCode(requestParameterResolver.resolveRequestParameter(webContext, OidcConstants.USER_CODE).orElse(null))
            .tenant(tenant)
            .build();
    }

    protected ResponseEntity buildCibaResponse(final OidcCibaRequest cibaRequest) {
        LoggingUtils.protocolMessage("OpenID Connect Backchannel Authentication Response",
            Map.of(
                "CAS Auth Request ID", cibaRequest.getId(),
                "Client Auth Request ID", cibaRequest.getEncodedId(),
                "Client ID", cibaRequest.getClientId(),
                "Scopes", cibaRequest.getScopes(),
                "Principal", cibaRequest.getAuthentication().getPrincipal().getId()));
        val cibaResponse = new OidcCibaResponse(cibaRequest.getEncodedId(), cibaRequest.getExpirationPolicy().getTimeToLive());
        return ResponseEntity.ok(cibaResponse);
    }


    protected OidcCibaRequest recordCibaRequest(final CibaRequestContext cibaRequestContext) throws Throwable {
        LoggingUtils.protocolMessage("OpenID Connect Backchannel Authentication Request",
            Map.of(
                "Client ID", cibaRequestContext.getClientId(),
                "Scopes", cibaRequestContext.getScope(),
                "Binding Message", StringUtils.defaultString(cibaRequestContext.getBindingMessage()),
                "Principal", cibaRequestContext.getPrincipal().getId()));
        val cibaFactory = (OidcCibaRequestFactory) configurationContext.getTicketFactory().get(OidcCibaRequest.class);
        val cibaRequest = cibaFactory.create(cibaRequestContext);
        configurationContext.getTicketRegistry().addTicket(cibaRequest);
        return cibaRequest;
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
        val cibaFactory = (OidcCibaRequestFactory) configurationContext.getTicketFactory().get(OidcCibaRequest.class);
        val decodedId = cibaFactory.decodeId(requestId);
        val cibaRequest = configurationContext.getTicketRegistry().getTicket(decodedId, OidcCibaRequest.class);
        Assert.notNull(cibaRequest, "CIBA request cannot be found");
        Assert.isTrue(cibaRequest.getEncodedId().equals(requestId), "CIBA request identifier is invalid");
        return cibaRequest;
    }
}
