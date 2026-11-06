package org.apereo.cas.oidc.web.controllers.ciba;

import module java.base;
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
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.net.URIBuilder;
import org.apereo.inspektr.audit.annotation.Audit;
import org.jspecify.annotations.Nullable;
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
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcCibaController}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Slf4j
@Tag(name = "OpenID Connect")
public class OidcCibaController extends BaseOidcController {
    private static final long MAX_REQUEST_OBJECT_LIFETIME_MINUTES = 60;

    private static final Duration MAX_REQUEST_OBJECT_LIFETIME = Duration.ofMinutes(MAX_REQUEST_OBJECT_LIFETIME_MINUTES);

    private static final String REQUEST_OBJECT_TICKET_PREFIX = "OidcCibaRequestObject:";

    private static final int REQUEST_OBJECT_LOCK_STRIPES = 64;

    private static final List<CasReentrantLock> REQUEST_OBJECT_LOCKS =
        Stream.generate(CasReentrantLock::new).limit(REQUEST_OBJECT_LOCK_STRIPES).toList();

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
        @PathVariable final String clientId,
        @PathVariable final String requestId) {
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
        @PathVariable final String clientId,
        @PathVariable final String requestId)
            throws Throwable {
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
     * Handle back-channel authn request, read as the authenticated client sent it.
     *
     * @param httpRequest the request
     * @param response    the response
     * @return the response entity
     */
    @PostMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.CIBA_URL,
        "/**/" + OidcConstants.CIBA_URL}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Audit(action = AuditableActions.OIDC_CIBA_RESPONSE,
        actionResolverName = AuditActionResolvers.OIDC_CIBA_RESPONSE_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.OIDC_CIBA_RESPONSE_RESOURCE_RESOLVER)
    @Operation(summary = "Handle back-channel authn request")
    public ResponseEntity handleBackchannelAuthnRequest(final HttpServletRequest httpRequest, final HttpServletResponse response) throws Throwable {
        val request = withAuthenticatedClientId(httpRequest, response);
        val cibaRequestContext = buildCibaRequestContext(request, response);

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
        val requestObjectError = validateRequestObject(request, registeredService);
        if (requestObjectError != null) {
            return badCibaRequest(requestObjectError);
        }
        if (!getConfigurationContext().getRequestParameterResolver().isAuthorizedResourceForService(request, registeredService)) {
            val body = OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_TARGET,
                "Requested resource is invalid or not authorized for this client");
            return ResponseEntity.badRequest().body(body);
        }
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
            || !Strings.CI.startsWith(registeredService.getBackchannelClientNotificationEndpoint(), "https://")) {
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

    protected CibaRequestContext buildCibaRequestContext(final HttpServletRequest request, final HttpServletResponse response) {
        val requestParameterResolver = getConfigurationContext().getRequestParameterResolver();
        val userProfile = OAuth20Utils.getAuthenticatedUserProfile(request, response, getConfigurationContext().getSessionStore());
        val clientId = Objects.requireNonNull(userProfile.getAttribute(OAuth20Constants.CLIENT_ID)).toString();

        val tenant = getConfigurationContext().getTenantExtractor().extract(request.getRequestURL().toString())
            .map(TenantDefinition::getId).orElse(StringUtils.EMPTY);

        return CibaRequestContext.builder()
            .acrValues(requestParameterResolver.resolveRequestParameters(request, OAuth20Constants.ACR_VALUES))
            .bindingMessage(requestParameterResolver.resolveRequestParameter(request, OidcConstants.BINDING_MESSAGE).orElse(null))
            .clientNotificationToken(requestParameterResolver.resolveRequestParameter(request, OidcConstants.CLIENT_NOTIFICATION_TOKEN).orElse(null))
            .idTokenHint(requestParameterResolver.resolveRequestParameter(request, OidcConstants.ID_TOKEN_HINT).orElse(null))
            .loginHint(requestParameterResolver.resolveRequestParameter(request, OidcConstants.LOGIN_HINT).orElse(null))
            .loginHintToken(requestParameterResolver.resolveRequestParameter(request, OidcConstants.LOGIN_HINT_TOKEN).orElse(null))
            .requestedExpiry(requestParameterResolver.resolveRequestParameter(request, OidcConstants.REQUESTED_EXPIRY, Long.class).orElse(0L))
            .scope(requestParameterResolver.resolveRequestScopes(request))
            .resources(requestParameterResolver.resolveRequestedResources(request))
            .clientId(clientId)
            .userCode(requestParameterResolver.resolveRequestParameter(request, OidcConstants.USER_CODE).orElse(null))
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
        val storedRequest = Objects.requireNonNull(configurationContext.getTicketRegistry().addTicket(cibaRequest),
            "Unable to store CIBA request");
        return storedRequest.isStateless()
            ? Objects.requireNonNull(configurationContext.getTicketRegistry().getTicket(storedRequest.getId(), OidcCibaRequest.class),
                "Unable to resolve CIBA request")
            : cibaRequest;
    }

    /**
     * The request as the authenticated client sent it. A request that names no {@code client_id}, as one authenticated
     * with HTTP Basic or a client assertion need not, is read as naming the authenticated client, so that its signed
     * authentication request is verified with the keys of that client and its parameters are read from it.
     *
     * @param request  the request
     * @param response the response
     * @return the request
     */
    protected HttpServletRequest withAuthenticatedClientId(final HttpServletRequest request, final HttpServletResponse response) {
        if (StringUtils.isNotBlank(request.getParameter(OAuth20Constants.CLIENT_ID))) {
            return request;
        }
        val userProfile = OAuth20Utils.getAuthenticatedUserProfile(request, response, getConfigurationContext().getSessionStore());
        val clientId = Objects.requireNonNull(userProfile.getAttribute(OAuth20Constants.CLIENT_ID)).toString();
        val parameterMap = new LinkedHashMap<>(request.getParameterMap());
        parameterMap.put(OAuth20Constants.CLIENT_ID, new String[]{clientId});
        return new HttpServletRequestWrapper(request) {
            @Override
            public String getParameter(final String name) {
                return OAuth20Constants.CLIENT_ID.equals(name) ? clientId : super.getParameter(name);
            }

            @Override
            public String[] getParameterValues(final String name) {
                return OAuth20Constants.CLIENT_ID.equals(name) ? new String[]{clientId} : super.getParameterValues(name);
            }

            @Override
            public Map<String, String[]> getParameterMap() {
                return parameterMap;
            }

            @Override
            public Enumeration<String> getParameterNames() {
                return Collections.enumeration(parameterMap.keySet());
            }
        };
    }

    /**
     * Validate the signed authentication request, if any (CIBA Core 1.0, section 7.1.1): it must verify with the keys of
     * the authenticated client, be signed with an asymmetric algorithm, name the client as its issuer and as its
     * {@code client_id} when it has one, be addressed to the issuer of CAS, and carry {@code exp}, {@code iat},
     * {@code nbf} and {@code jti}, with {@code exp} in the future and {@code iat} and {@code nbf} in the past, within the
     * allowed skew. It may be valid for {@value #MAX_REQUEST_OBJECT_LIFETIME_MINUTES} minutes at most from its
     * {@code iat} and its {@code nbf}, as FAPI CIBA allows, and is used once: its {@code jti} is kept in the ticket
     * registry until it expires, beyond the allowed skew; checking and recording it is a single step on each node. When
     * CAS does not support the request parameter, a request object is refused, since it could not be verified. A request
     * object that is not valid is refused rather than set aside for the parameters of the request.
     *
     * @param request           the request
     * @param registeredService the authenticated client
     * @return the error, or null when the request carries no request object or a valid one
     */
    protected @Nullable String validateRequestObject(final HttpServletRequest request, final OidcRegisteredService registeredService) {
        val requestObject = request.getParameter(OAuth20Constants.REQUEST);
        if (StringUtils.isBlank(requestObject)) {
            return null;
        }
        if (!getConfigurationContext().getCasProperties().getAuthn().getOidc().getDiscovery().isRequestParameterSupported()) {
            return "Signed authentication requests are not supported";
        }
        val clientId = registeredService.getClientId();
        val requestParameterResolver = getConfigurationContext().getRequestParameterResolver();
        if (!requestParameterResolver.isRequestObjectVerified(request)
            || !requestParameterResolver.resolveRequestParameter(request, OAuth20Constants.CLIENT_ID).map(clientId::equals).orElse(Boolean.TRUE)) {
            return "Request object cannot be verified or is not for client " + clientId;
        }
        try {
            if (!(JWTParser.parse(requestObject) instanceof final SignedJWT signedJwt)
                || !JWSAlgorithm.Family.SIGNATURE.contains(signedJwt.getHeader().getAlgorithm())) {
                return "Request object must be signed with an asymmetric algorithm";
            }
            val claims = signedJwt.getJWTClaimsSet();
            val issuer = getConfigurationContext().getIssuerService().determineIssuer(Optional.of(registeredService));
            val skew = Beans.newDuration(getConfigurationContext().getCasProperties().getAuthn().getOidc().getCore().getSkew());
            val now = Instant.now(Clock.systemUTC());
            if (!clientId.equals(claims.getIssuer())) {
                return "Request object must be issued by client " + clientId;
            }
            if (claims.getAudience() == null || !claims.getAudience().contains(issuer)) {
                return "Request object must be addressed to " + issuer;
            }
            if (claims.getIssueTime() == null || StringUtils.isBlank(claims.getJWTID())) {
                return "Request object must carry iat and jti";
            }
            if (claims.getExpirationTime() == null || !claims.getExpirationTime().toInstant().plus(skew).isAfter(now)) {
                return "Request object has no expiration or has expired";
            }
            if (claims.getNotBeforeTime() == null || claims.getNotBeforeTime().toInstant().minus(skew).isAfter(now)) {
                return "Request object has no nbf or is not valid yet";
            }
            if (claims.getIssueTime().toInstant().minus(skew).isAfter(now)) {
                return "Request object is issued in the future";
            }
            val expiration = claims.getExpirationTime().toInstant();
            if (expiration.isAfter(claims.getIssueTime().toInstant().plus(MAX_REQUEST_OBJECT_LIFETIME))
                || expiration.isAfter(claims.getNotBeforeTime().toInstant().plus(MAX_REQUEST_OBJECT_LIFETIME))) {
                return "Request object may be valid for %s minutes at most".formatted(MAX_REQUEST_OBJECT_LIFETIME_MINUTES);
            }
            return isFirstUse(clientId, claims.getJWTID(), expiration.plus(skew)) ? null : "Request object jti has already been used";
        } catch (final ParseException e) {
            LOGGER.debug("Request object cannot be parsed", e);
            return "Request object cannot be parsed";
        }
    }

    /**
     * Record the use of a signed authentication request, unless it was used before. Checking and recording is a single
     * step on this node, under a lock striped by the request object; nodes that share the ticket registry may still each
     * accept the same request object once at the very same moment.
     *
     * @param clientId the client id
     * @param jwtId    the jti of the request object
     * @param keepTill until when the use is kept
     * @return true when it was not used before
     */
    protected boolean isFirstUse(final String clientId, final String jwtId, final Instant keepTill) {
        val ticketId = TransientSessionTicketFactory.normalizeTicketId(REQUEST_OBJECT_TICKET_PREFIX + clientId + ':' + DigestUtils.sha256(jwtId));
        return REQUEST_OBJECT_LOCKS.get(Math.floorMod(ticketId.hashCode(), REQUEST_OBJECT_LOCKS.size()))
            .execute(() -> recordFirstUse(clientId, ticketId, keepTill));
    }

    private boolean recordFirstUse(final String clientId, final String ticketId, final Instant keepTill) {
        val ticketRegistry = getConfigurationContext().getTicketRegistry();
        try {
            val used = ticketRegistry.getTicket(ticketId, TransientSessionTicket.class);
            if (used != null && !used.isExpired()) {
                return false;
            }
        } catch (final InvalidTicketException e) {
            LOGGER.trace("Request object [{}] has not been used", ticketId);
        }
        val factory = (TransientSessionTicketFactory) getConfigurationContext().getTicketFactory().get(TransientSessionTicket.class);
        val lifetime = Math.max(1, Duration.between(Instant.now(Clock.systemUTC()), keepTill).toSeconds());
        val properties = new HashMap<String, Serializable>();
        properties.put(OAuth20Constants.CLIENT_ID, clientId);
        properties.put(ExpirationPolicy.class.getName(), new HardTimeoutExpirationPolicy(lifetime));
        FunctionUtils.doUnchecked(_ -> ticketRegistry.addTicket(factory.create(ticketId, properties)));
        return true;
    }

    protected ResponseEntity badCibaRequest(final String error) {
        val body = OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, error);
        return ResponseEntity.badRequest().body(body);
    }

    protected @Nullable Principal determineCibaRequestPrincipal(final CibaRequestContext cibaRequest,
                                                                final OidcRegisteredService registeredService)
            throws Throwable {
        var subject = cibaRequest.getLoginHint();
        if (StringUtils.isNotBlank(cibaRequest.getIdTokenHint())) {
            val claims = configurationContext.getIdTokenSigningAndEncryptionService()
                .decode(cibaRequest.getIdTokenHint(), Optional.of(registeredService));
            subject = claims.getSubject();
        }
        if (StringUtils.isNotBlank(cibaRequest.getLoginHintToken())) {
            subject = JwtBuilder.tryParse(cibaRequest.getLoginHint()).map(JWTClaimsSet::getSubject).orElse(null);
        }
        if (StringUtils.isNotBlank(subject)) {
            return configurationContext.getPrincipalResolver().resolve(new BasicIdentifiableCredential(subject));
        }
        return null;
    }

    /**
     * Fetch the request by the id given to the client. A stateless request is given its own id,
     * which is looked up as is; other ids are decoded first, and must match the encoded id of the request.
     *
     * @param requestId the request id
     * @return the request
     */
    private OidcCibaRequest fetchOidcCibaRequest(final String requestId) {
        val cibaFactory = (OidcCibaRequestFactory) configurationContext.getTicketFactory().get(OidcCibaRequest.class);
        val decodedId = Strings.CS.startsWith(requestId, OidcCibaRequest.PREFIX + UniqueTicketIdGenerator.SEPARATOR)
            ? requestId
            : cibaFactory.decodeId(requestId);
        val cibaRequest = configurationContext.getTicketRegistry().getTicket(decodedId, OidcCibaRequest.class);
        Assert.notNull(cibaRequest, "CIBA request cannot be found");
        Assert.isTrue(cibaRequest.getEncodedId().equals(requestId), "CIBA request identifier is invalid");
        return cibaRequest;
    }
}
