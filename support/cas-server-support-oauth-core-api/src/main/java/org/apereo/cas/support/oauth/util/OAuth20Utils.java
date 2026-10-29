package org.apereo.cas.support.oauth.util;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.services.query.RegisteredServiceQuery;
import org.apereo.cas.support.oauth.OAuth20ClientAuthenticationMethods;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.services.RegisteredServiceOAuthResourceMatcher;
import org.apereo.cas.support.oauth.validator.DPoPBoundAccessTokenDowngradeException;
import org.apereo.cas.support.oauth.validator.OAuth20ProofOfPossessionValidator;
import org.apereo.cas.support.oauth.validator.token.OAuth20InvalidTargetException;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGeneratedResult;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.CasSessionStore;
import org.apereo.cas.web.flow.CasWebflowConstants;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.oauth2.sdk.client.RedirectURIValidator;
import com.nimbusds.oauth2.sdk.dpop.verifiers.InvalidDPoPNonceException;
import com.nimbusds.oauth2.sdk.token.DPoPTokenError;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.JacksonJsonView;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.cert.X509Certificate;

/**
 * This class has some useful methods to output data in plain text,
 * handle redirects, add parameter in url or find the right provider.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Slf4j
@UtilityClass
public class OAuth20Utils {
    /**
     * Property of the redeemed-code record that lists the tokens issued from the code.
     */
    public static final String REDEEMED_CODE_TOKENS = "oauthTokensIssuedFromCode";

    private static final String X509_CERTIFICATE_REQUEST_ATTRIBUTE = "jakarta.servlet.request.X509Certificate";

    private static final String BASIC_AUTHORIZATION_PREFIX = "Basic ";

    private static final List<String> STATIC_RESOURCES = List.of(".css", ".js", ".png", ".jpg", ".ico", ".jpeg", ".bmp", ".gif");

    private static final long STRICT_TRANSPORT_SECURITY_MAX_AGE = 15_768_000L;

    private static final String REQUEST_ATTRIBUTE_CLIENT_ID_BY_REDIRECT_URI = OAuth20Utils.class.getName() + ".clientId";

    private static final JsonMapper MAPPER = JacksonObjectMapperFactory.builder()
        .singleArrayElementUnwrapped(true).build().toJsonMapper();

    private static final JsonMapper UNTYPED_MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toJsonMapper();

    /**
     * Write to the output this error.
     *
     * @param response the response
     * @param error    error message
     * @return json -backed view.
     */
    public static ModelAndView writeError(final HttpServletResponse response, final String error) {
        return writeError(response, error, null);
    }

    /**
     * Write error model and view.
     *
     * @param response    the response
     * @param error       the error
     * @param description the description
     * @return the model and view
     */
    public static ModelAndView writeError(final HttpServletResponse response,
                                          final String error,
                                          @Nullable final String description) {
        val model = getErrorResponseBody(error, description);
        val mv = new ModelAndView(new JacksonJsonView(MAPPER), model);
        mv.setStatus(HttpStatus.BAD_REQUEST);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return mv;
    }

    /**
     * Gets error response body.
     *
     * @param error       the error
     * @param description the description
     * @return the error response body
     */
    public static Map<String, Object> getErrorResponseBody(final String error, @Nullable final String description) {
        val model = CollectionUtils.<String, Object>wrap(OAuth20Constants.ERROR, error);
        if (StringUtils.isNotBlank(description)) {
            model.put(OAuth20Constants.ERROR_DESCRIPTION, description);
        }
        return model;
    }

    /**
     * The answer of a protected resource that requires the server-provided nonce in DPoP proofs (RFC 9449, section 9):
     * {@code 401} with a {@code WWW-Authenticate: DPoP} challenge carrying {@code use_dpop_nonce} and the accepted
     * algorithms. The fresh nonce is already in the {@code DPoP-Nonce} header of the response.
     *
     * @param algorithms the JWS algorithms accepted for DPoP proofs
     * @return the response entity
     */
    public static ResponseEntity useDPoPNonceResponse(final Set<JWSAlgorithm> algorithms) {
        val error = DPoPTokenError.USE_DPOP_NONCE.setJWSAlgorithms(algorithms.isEmpty() ? null : algorithms);
        return ResponseEntity.status(error.getHTTPStatusCode())
            .header(HttpHeaders.WWW_AUTHENTICATE, error.toWWWAuthenticateHeader())
            .body(getErrorResponseBody(error.getCode(), error.getDescription()));
    }

    /**
     * The {@code algs} parameter of a {@code DPoP} challenge, which lists the JWS algorithms accepted for DPoP proofs
     * (RFC 9449, section 7.1).
     *
     * @param algorithms the accepted algorithms
     * @return the parameter, or empty when no algorithm is accepted
     */
    public static Optional<String> toDPoPAlgorithmsChallengeParameter(final Collection<JWSAlgorithm> algorithms) {
        return algorithms.isEmpty()
            ? Optional.empty()
            : Optional.of("algs=\"%s\"".formatted(algorithms.stream().map(JWSAlgorithm::getName).collect(Collectors.joining(" "))));
    }

    /**
     * Gets registered oauth service by client id.
     *
     * @param <T>             the type parameter
     * @param servicesManager the services manager
     * @param clientId        the client id
     * @param clazz           the clazz
     * @return the registered o auth service by client id
     */
    public static <T extends OAuthRegisteredService> @Nullable T getRegisteredOAuthServiceByClientId(
        final ServicesManager servicesManager, final String clientId, final Class<T> clazz) {
        return FunctionUtils.doIfNotBlank(clientId,
            () -> {
                val query = RegisteredServiceQuery.of(OAuthRegisteredService.class, "clientId", clientId).withIncludeAssignableTypes(true);
                return servicesManager.findServicesBy(query).findFirst().map(clazz::cast).orElse(null);
            });
    }

    /**
     * Locate the requested instance of {@link OAuthRegisteredService} by the given clientId.
     *
     * @param servicesManager the service registry DAO instance.
     * @param clientId        the client id by which the {@link OAuthRegisteredService} is to be located.
     * @return null, or the located {@link OAuthRegisteredService} instance in the service registry.
     */
    public static @Nullable OAuthRegisteredService getRegisteredOAuthServiceByClientId(final ServicesManager servicesManager,
                                                                                       final String clientId) {
        return getRegisteredOAuthServiceByClientId(servicesManager, clientId, OAuthRegisteredService.class);
    }

    /**
     * Gets registered oauth service by redirect uri.
     *
     * @param servicesManager the services manager
     * @param redirectUri     the redirect uri
     * @return the registered OAuth service by redirect uri
     */
    public static @Nullable OAuthRegisteredService getRegisteredOAuthServiceByRedirectUri(final ServicesManager servicesManager,
                                                                                          final String redirectUri) {
        return validateRedirectUri(redirectUri, false)
            ? getRegisteredOAuthServiceByPredicate(servicesManager, service -> service.matches(redirectUri))
            : null;
    }

    private static @Nullable OAuthRegisteredService getRegisteredOAuthServiceByPredicate(final ServicesManager servicesManager,
                                                                                         final Predicate<OAuthRegisteredService> predicate) {
        val services = servicesManager.getAllServicesOfType(OAuthRegisteredService.class);
        return services
            .stream()
            .filter(predicate)
            .findFirst()
            .orElse(null);
    }

    /**
     * Produce unauthorized error view model and view.
     *
     * @return the model and view
     */
    public static ModelAndView produceUnauthorizedErrorView() {
        return produceUnauthorizedErrorView(HttpStatus.UNAUTHORIZED);
    }

    /**
     * Produce unauthorized error view.
     *
     * @param status the status
     * @return the model and view
     */
    public static ModelAndView produceUnauthorizedErrorView(final HttpStatus status) {
        val ex = UnauthorizedServiceException.denied("Rejected: %s".formatted(status));
        return produceErrorView(ex, status);
    }

    /**
     * Produce error view model and view.
     *
     * @param e the e
     * @return the model and view
     */
    public static ModelAndView produceErrorView(final Exception e) {
        return produceErrorView(e, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Produce error view.
     *
     * @param e      the exception
     * @param status the status
     * @return the model and view
     */
    public static ModelAndView produceErrorView(final Exception e, final HttpStatus status) {
        val mv = new ModelAndView(CasWebflowConstants.VIEW_ID_SERVICE_ERROR,
            CollectionUtils.wrap(CasWebflowConstants.ATTRIBUTE_ERROR_ROOT_CAUSE_EXCEPTION, e));
        mv.setStatus(status);
        return mv;
    }

    /**
     * CAS oauth callback url.
     *
     * @param serverPrefixUrl the server prefix url
     * @return the string
     */
    public static String casOAuthCallbackUrl(final String serverPrefixUrl) {
        return serverPrefixUrl.concat(OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.CALLBACK_AUTHORIZE_URL);
    }

    /**
     * Jsonify.
     *
     * @param value the map
     * @return the string
     */
    public static String toJson(final Object value) {
        return MAPPER.writeValueAsString(value);
    }

    /**
     * Check the grant type against an expected grant type.
     *
     * @param type         the given grant type
     * @param expectedType the expected grant type
     * @return whether the grant type is the expected one
     */
    public static boolean isGrantType(final String type, final OAuth20GrantTypes expectedType) {
        return expectedType.getType().equalsIgnoreCase(type);
    }

    /**
     * Check the response type against an expected response type.
     *
     * @param type         the given response type
     * @param expectedType the expected response type
     * @return whether the response type is the expected one
     */
    public static boolean isResponseType(final String type, final OAuth20ResponseTypes expectedType) {
        return expectedType.getType().equalsIgnoreCase(type);
    }

    /**
     * Determines whether the response mode type matches the expected type.
     *
     * @param type         the type
     * @param expectedType the expected type
     * @return true/false
     */
    public static boolean isResponseModeType(final String type, final OAuth20ResponseModeTypes expectedType) {
        return expectedType.getType().equalsIgnoreCase(type);
    }

    /**
     * Gets service request header if any.
     *
     * @param request the request
     * @return the service request header if any
     */
    public static String getServiceRequestHeaderIfAny(final HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(CasProtocolConstants.PARAMETER_SERVICE))
            .or(() -> Optional.ofNullable(request.getHeader("X-".concat(CasProtocolConstants.PARAMETER_SERVICE))))
            .orElse(StringUtils.EMPTY);
    }

    /**
     * Check if the callback url is valid.
     *
     * @param registeredService the registered service
     * @param redirectUri       the callback url
     * @return whether the callback url is valid
     */
    public static boolean checkCallbackValid(final @NonNull RegisteredService registeredService,
                                             final String redirectUri) {
        val validRedirectUri = validateRedirectUri(redirectUri, false);
        val matchingStrategy = Optional.of(registeredService)
            .map(RegisteredService::getMatchingStrategy)
            .orElse(null);
        if (!validRedirectUri || matchingStrategy == null || !matchingStrategy.matches(registeredService, redirectUri)) {
            LOGGER.warn("Unsupported [{}]: [{}] does not match what is defined for registered service: [{}]. "
                    + "Service is considered unauthorized. Verify the service matching strategy used in the service "
                    + "definition is correct and does in fact match the client [{}]",
                OAuth20Constants.REDIRECT_URI, redirectUri, registeredService.getServiceId(), redirectUri);
            return false;
        }
        return true;
    }


    /**
     * Check the response type against expected response types.
     *
     * @param type          the current response type
     * @param expectedTypes the expected response types
     * @return whether the response type is supported
     */
    public static boolean checkResponseTypes(final String type, final OAuth20ResponseTypes... expectedTypes) {
        LOGGER.debug("Response type: [{}]", type);
        val checked = Stream.of(expectedTypes).anyMatch(t -> OAuth20Utils.isResponseType(type, t));
        if (!checked) {
            LOGGER.error("Unsupported response type: [{}]", type);
        }
        return checked;
    }

    /**
     * Gets client id from authenticated profile.
     *
     * @param profile the profile
     * @return the client id from authenticated profile
     */
    public static @Nullable String getClientIdFromAuthenticatedProfile(final OAuth20UserProfile profile) {
        val attrs = new HashMap<>(profile.getAttributes());
        if (attrs.containsKey(OAuth20Constants.CLIENT_ID)) {
            val attribute = attrs.get(OAuth20Constants.CLIENT_ID);
            return CollectionUtils.toCollection(attribute, ArrayList.class).getFirst().toString();
        }
        return null;
    }

    /**
     * Parse user info request claims set.
     *
     * @param token the token
     * @return the set
     */
    public static Set<String> parseUserInfoRequestClaims(final OAuth20Token token) {
        return token != null ? token.getClaims().getOrDefault("userinfo", new HashMap<>()).keySet() : new HashSet<>();
    }

    /**
     * Get the OAuth token named in the current request. The token is read from the ticket registry once per request
     * and reused by every authenticator, validator and extractor that handles the same request.
     *
     * @param <T>            the token type
     * @param request        the request
     * @param ticketRegistry the ticket registry
     * @param tokenId        the token id
     * @param clazz          the expected token type
     * @return the token, or null if it cannot be found or is of another type
     */
    public static <T extends OAuth20Token> @Nullable T getRequestedOAuthToken(final HttpServletRequest request,
                                                                              final TicketRegistry ticketRegistry,
                                                                              final String tokenId,
                                                                              final Class<T> clazz) {
        if (StringUtils.isBlank(tokenId)) {
            return null;
        }
        val attributeName = OAuth20Token.class.getName() + '.' + tokenId;
        val token = Optional.ofNullable(request.getAttribute(attributeName))
            .or(() -> Optional.ofNullable(FunctionUtils.doAndHandle(() -> ticketRegistry.getTicket(tokenId, OAuth20Token.class))))
            .orElse(null);
        if (token != null) {
            request.setAttribute(attributeName, token);
        }
        return clazz.isInstance(token) ? clazz.cast(token) : null;
    }

    /**
     * The id of the ticket that records the tokens issued from an authorization code once it is redeemed.
     * The code itself is removed when it is redeemed, so this record is what a later presentation of the code finds.
     *
     * @param code the authorization code
     * @return the record ticket id
     */
    public static String getRedeemedCodeTicketId(final String code) {
        return TransientSessionTicketFactory.normalizeTicketId(DigestUtils.sha256(code));
    }

    /**
     * Revoke the tokens issued from an authorization code that is presented again after it was redeemed
     * (RFC 6749, section 4.1.2): the access and refresh tokens recorded when the code was redeemed, and the access
     * tokens later issued from that refresh token.
     *
     * @param ticketRegistry the ticket registry
     * @param code           the authorization code presented again
     * @return the number of tokens revoked
     */
    public static long revokeTokensIssuedFromCode(final TicketRegistry ticketRegistry, final String code) {
        if (StringUtils.isBlank(code)) {
            return 0;
        }
        return FunctionUtils.doAndHandle(() -> revokeRecordedTokens(ticketRegistry, code), e -> {
            LOGGER.warn("Unable to revoke tokens issued from authorization code [{}]: [{}]", code, e.getMessage());
            return 0L;
        }).get();
    }

    private static long revokeRecordedTokens(final TicketRegistry ticketRegistry, final String code) throws Exception {
        val recordId = getRedeemedCodeTicketId(code);
        if (!(ticketRegistry.getTicket(recordId) instanceof final TransientSessionTicket redeemedCode)) {
            return 0L;
        }
        val tokens = Objects.requireNonNullElseGet(redeemedCode.getProperty(REDEEMED_CODE_TOKENS, List.class), List::of);
        var revoked = 0L;
        for (val token : tokens) {
            val tokenId = token.toString();
            if (ticketRegistry.getTicket(tokenId) instanceof final OAuth20RefreshToken refreshToken) {
                for (val accessTokenId : refreshToken.getAccessTokens()) {
                    revoked += ticketRegistry.deleteTicket(accessTokenId);
                }
            }
            revoked += ticketRegistry.deleteTicket(tokenId);
        }
        ticketRegistry.deleteTicket(recordId);
        LOGGER.warn("Authorization code [{}] was presented again after it was redeemed; revoked [{}] token(s) issued from it", code, revoked);
        return revoked;
    }

    /**
     * Gets authenticated user profile.
     *
     * @param request      the request
     * @param response     the response
     * @param sessionStore the session store
     * @return the authenticated user profile
     */
    public OAuth20UserProfile getAuthenticatedUserProfile(final HttpServletRequest request, final HttpServletResponse response,
                                                          final CasSessionStore sessionStore) {
        return findAuthenticatedUserProfile(request, response, sessionStore)
            .orElseThrow(() -> new IllegalArgumentException("Unable to determine the user profile from the request"));
    }

    /**
     * Finds the authenticated user profile, the one that client authentication, an access token or the login handshake
     * keeps for the request or, failing that, the one the login handshake keeps in the session.
     *
     * @param request      the request
     * @param response     the response
     * @param sessionStore the session store
     * @return the authenticated user profile, if any
     */
    public Optional<OAuth20UserProfile> findAuthenticatedUserProfile(final HttpServletRequest request, final HttpServletResponse response,
                                                                     final CasSessionStore sessionStore) {
        if (request.getAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_AUTHENTICATED_PROFILE) instanceof final OAuth20UserProfile profile) {
            return Optional.of(profile);
        }
        return sessionStore.get(request, OAuth20Constants.SESSION_ATTRIBUTE_AUTHENTICATED_PROFILE)
            .filter(OAuth20UserProfile.class::isInstance)
            .map(OAuth20UserProfile.class::cast);
    }

    /**
     * Read the client id and secret of the HTTP Basic {@code Authorization} header, the way client authentication reads
     * them: the header must start with {@code Basic }, in any case (RFC 9110, section 11.1), and decode to {@code id:secret}.
     *
     * @param request the request
     * @return the client id and secret, if the request carries the header
     * @throws BadCredentialsException when the header is not a Basic one or cannot be decoded
     */
    public Optional<Pair<String, String>> readBasicAuthorization(final HttpServletRequest request) throws BadCredentialsException {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null) {
            return Optional.empty();
        }
        if (!Strings.CI.startsWith(header, BASIC_AUTHORIZATION_PREFIX)) {
            throw new BadCredentialsException("Wrong prefix for header: " + HttpHeaders.AUTHORIZATION);
        }
        try {
            val token = new String(Base64.getDecoder().decode(header.substring(BASIC_AUTHORIZATION_PREFIX.length())), StandardCharsets.UTF_8);
            val delimiter = token.indexOf(':');
            if (delimiter < 0) {
                throw new BadCredentialsException("Bad format of the basic auth header");
            }
            return Optional.of(Pair.of(token.substring(0, delimiter), token.substring(delimiter + 1)));
        } catch (final IllegalArgumentException e) {
            throw new BadCredentialsException("Bad format of the basic auth header", e);
        }
    }

    /**
     * Read the client id and secret of the HTTP Basic {@code Authorization} header of a client authentication request.
     * Both are form-url-decoded, as clients encode them before HTTP Basic encoding (RFC 6749, section 2.3.1).
     *
     * @param request the request
     * @return the client id and secret, if the request carries the header
     * @throws BadCredentialsException when the header is not a Basic one or cannot be decoded
     */
    public Optional<Pair<String, String>> readClientBasicAuthorization(final HttpServletRequest request) throws BadCredentialsException {
        return readBasicAuthorization(request).map(credentials -> {
            try {
                return Pair.of(URLDecoder.decode(credentials.getLeft(), StandardCharsets.UTF_8),
                    URLDecoder.decode(credentials.getRight(), StandardCharsets.UTF_8));
            } catch (final IllegalArgumentException e) {
                throw new BadCredentialsException("Client credentials of the basic auth header are not form-url-encoded", e);
            }
        });
    }

    /**
     * The client a request is made for: its {@code client_id} parameter, or else the client named by its HTTP Basic
     * credentials or by its client assertion, or else the client whose redirect URI the request carries.
     * A client that authenticates with HTTP Basic, as a confidential client usually does at the token endpoint,
     * sends no {@code client_id} parameter; its credentials name it, as does the assertion of a client that authenticates
     * with {@code private_key_jwt} or {@code client_secret_jwt} (RFC 7523, section 3; OpenID Connect Core, section 9: its
     * {@code iss} and {@code sub} are both the client id). The redirect URI lookup is kept for the request.
     *
     * @param request                  the request
     * @param servicesManager          the services manager
     * @param requestParameterResolver the request parameter resolver
     * @return the client id, or an empty string
     */
    public String resolveClientIdFromRequest(final HttpServletRequest request, final ServicesManager servicesManager,
                                             final OAuth20RequestParameterResolver requestParameterResolver) {
        val clientId = requestParameterResolver.resolveRequestParameter(request, OAuth20Constants.CLIENT_ID)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        if (StringUtils.isNotBlank(clientId)) {
            return clientId;
        }
        val redirectUri = requestParameterResolver.resolveRequestParameter(request, OAuth20Constants.REDIRECT_URI)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        validateRedirectUri(redirectUri, true);
        return resolveClientIdFromBasicAuthentication(request, requestParameterResolver)
            .or(() -> resolveClientIdFromAssertion(request, requestParameterResolver))
            .orElseGet(() -> resolveClientIdByRedirectUri(request, servicesManager, redirectUri));
    }

    private static Optional<String> resolveClientIdFromBasicAuthentication(final HttpServletRequest request,
                                                                           final OAuth20RequestParameterResolver requestParameterResolver) {
        val authorization = Objects.requireNonNullElse(request.getHeader(HttpHeaders.AUTHORIZATION), StringUtils.EMPTY);
        if (!Strings.CI.startsWith(authorization, BASIC_AUTHORIZATION_PREFIX)) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(requestParameterResolver.resolveClientIdAndClientSecret(request).getLeft())
                .filter(StringUtils::isNotBlank);
        } catch (final BadCredentialsException e) {
            LOGGER.debug("Unable to read client credentials from the authorization header: [{}]", e.getMessage());
            return Optional.empty();
        }
    }

    private static Optional<String> resolveClientIdFromAssertion(final HttpServletRequest request,
                                                                 final OAuth20RequestParameterResolver requestParameterResolver) {
        val assertionType = requestParameterResolver.resolveRequestParameter(request, OAuth20Constants.CLIENT_ASSERTION_TYPE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val assertion = requestParameterResolver.resolveRequestParameter(request, OAuth20Constants.CLIENT_ASSERTION)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        if (!OAuth20Constants.CLIENT_ASSERTION_TYPE_JWT_BEARER.equals(assertionType) || StringUtils.isBlank(assertion)) {
            return Optional.empty();
        }
        try {
            val claims = JWTParser.parse(assertion).getJWTClaimsSet();
            return Optional.ofNullable(claims)
                .map(JWTClaimsSet::getSubject)
                .filter(StringUtils::isNotBlank)
                .filter(subject -> subject.equals(claims.getIssuer()));
        } catch (final Exception e) {
            LOGGER.debug("Unable to read the client assertion to determine the client: [{}]", e.getMessage());
            return Optional.empty();
        }
    }

    private static String resolveClientIdByRedirectUri(final HttpServletRequest request, final ServicesManager servicesManager,
                                                       final String redirectUri) {
        val attributeName = REQUEST_ATTRIBUTE_CLIENT_ID_BY_REDIRECT_URI + ':' + redirectUri;
        val resolvedClientId = request.getAttribute(attributeName);
        if (resolvedClientId != null) {
            return resolvedClientId.toString();
        }
        val svc = getRegisteredOAuthServiceByRedirectUri(servicesManager, redirectUri);
        val clientId = svc != null ? svc.getClientId() : StringUtils.EMPTY;
        request.setAttribute(attributeName, clientId);
        return clientId;
    }

    /**
     * Determines whether the registered service requires authentication.
     *
     * @param registeredService the registered service
     * @return whether the service need authentication
     */
    public boolean doesServiceNeedAuthentication(final OAuthRegisteredService registeredService) {
        return registeredService.getClientSecrets() != null
            && !registeredService.getClientSecrets().isEmpty()
            && registeredService.getClientSecrets().stream().anyMatch(secret -> StringUtils.isNotBlank(secret.getValue()));
    }

    /**
     * A public client has no means of authenticating at the token endpoint: no client secret, and no token endpoint
     * authentication method other than {@code none}.
     *
     * @param registeredService the registered service
     * @return true when the client is public
     */
    public boolean isPublicClient(final OAuthRegisteredService registeredService) {
        val authenticationMethod = registeredService.getTokenEndpointAuthenticationMethod();
        return !doesServiceNeedAuthentication(registeredService)
            && (StringUtils.isBlank(authenticationMethod)
            || Strings.CI.equals(authenticationMethod, OAuth20ClientAuthenticationMethods.NONE.getType()));
    }

    /**
     * Validate redirect uri.
     *
     * @param redirectUri the redirect uri
     */
    public boolean validateRedirectUri(final String redirectUri, final boolean throwOnFailure) {
        return StringUtils.isNotBlank(redirectUri) && FunctionUtils.doAndHandle(() -> {
            RedirectURIValidator.ensureLegal(URI.create(redirectUri));
            return true;
        }, e -> {
            if (throwOnFailure) {
                throw e;
            }
            return false;
        }).get();
    }


    /**
     * Is access token request?.
     *
     * @param request the request
     * @return true or false
     */
    public static boolean isAccessTokenRequest(final HttpServletRequest request) {
        return Boolean.TRUE.equals(request.getAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_ACCESS_TOKEN_REQUEST));
    }

    /**
     * Whether the request authenticates its client: an access token request, or a request to the revocation, pushed
     * authorization, backchannel authentication or verifiable credential endpoints. Such requests use the client
     * authentication method the client registered for the token endpoint (RFC 9126, section 2; CIBA, section 7.1).
     *
     * @param request the request
     * @return true or false
     */
    public static boolean isClientAuthenticationRequest(final HttpServletRequest request) {
        return isAccessTokenRequest(request)
            || Boolean.TRUE.equals(request.getAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_CLIENT_AUTHENTICATION_REQUEST));
    }

    /**
     * Is token authentication method supported for service.
     *
     * @param request              the request
     * @param registeredService    the registered service
     * @param authenticationMethod the authentication method
     * @return true/false
     */
    public static boolean isTokenAuthenticationMethodSupportedFor(final HttpServletRequest request,
                                                                  final OAuthRegisteredService registeredService,
                                                                  final OAuth20ClientAuthenticationMethods... authenticationMethod) {
        return !OAuth20Utils.isClientAuthenticationRequest(request)
            || StringUtils.isBlank(registeredService.getTokenEndpointAuthenticationMethod())
            || Arrays.stream(authenticationMethod).anyMatch(method -> Strings.CI.equals(registeredService.getTokenEndpointAuthenticationMethod(), method.getType()));
    }

    /**
     * Find stateless ticket validation result.
     *
     * @param profile the profile
     * @return the ticket validation result
     */
    public static Boolean isStatelessAuthentication(final OAuth20UserProfile profile) {
        val validationResult = (Boolean) profile.getAttribute(OAuth20Constants.CAS_OAUTH_STATELESS_PROPERTY);
        val principal = profile.getAttribute(Principal.class.getName());
        return validationResult != null && validationResult && principal != null;
    }

    /**
     * Gets access token timeout (in seconds).
     *
     * @param accessTokenResult the access token result
     * @return the access token timeout
     */
    public static Long getAccessTokenTimeout(final OAuth20TokenGeneratedResult accessTokenResult) {
        return accessTokenResult
            .getAccessToken()
            .map(token -> {
                if (token.isStateless()) {
                    val duration = Duration.between(ZonedDateTime.now(Clock.systemUTC()),
                        token.getExpirationPolicy().toMaximumExpirationTime(token));
                    return duration.toSeconds();
                }
                return ((OAuth20AccessToken) token).getExpiresIn();
            })
            .orElse(0L);
    }

    /**
     * Extract client id from token.
     *
     * @param token the token
     * @return the string
     * @throws Exception the exception
     */
    public static String extractClientIdFromToken(final String token) throws Exception {
        val claims = JwtBuilder.parse(token);
        if (claims != null) {
            return claims.getClaimAsString(OAuth20Constants.CLIENT_ID);
        }
        val header = JwtBuilder.parseHeader(token);
        return (String) header.getCustomParam(OAuth20Constants.CLIENT_ID);
    }

    /**
     * Compute the certificate thumbprint for certificate-bound tokens per RFC 8705,
     * which is the base64url-encoded SHA-256 hash of the DER-encoded certificate.
     *
     * @param certificate the certificate
     * @return the thumbprint
     */
    public static String computeCertificateThumbprint(final X509Certificate certificate) {
        return FunctionUtils.doUnchecked(() -> EncodingUtils.encodeUrlSafeBase64(DigestUtils.rawDigest("SHA-256", certificate.getEncoded())));
    }

    /**
     * The thumbprint of the client certificate an access token is bound to (RFC 8705, section 3). It is recorded on the
     * token's authentication, and, for tokens issued before that or to the client itself, on its principal.
     *
     * @param accessToken the access token
     * @return the certificate thumbprint, empty when the token is not certificate-bound
     */
    public static Optional<String> getBoundCertificateThumbprint(final OAuth20Token accessToken) {
        val authentication = accessToken.getAuthentication();
        return Stream.of(authentication.getAttributes(), authentication.getPrincipal().getAttributes())
            .map(attributes -> attributes.get(OAuth20Constants.X509_CERTIFICATE_DIGEST))
            .filter(Objects::nonNull)
            .map(CollectionUtils::firstElement)
            .flatMap(Optional::stream)
            .map(Object::toString)
            .findFirst();
    }

    /**
     * Whether the request presents, over mutual TLS, the client certificate with the given thumbprint. The certificate is
     * the one the TLS layer exposes on the request, the same one client authentication at the token endpoint uses.
     *
     * @param request               the request
     * @param certificateThumbprint the thumbprint of the bound certificate
     * @return true when the presented certificate matches
     */
    public static boolean isBoundCertificatePresented(final HttpServletRequest request, final String certificateThumbprint) {
        return Optional.ofNullable((X509Certificate[]) request.getAttribute(X509_CERTIFICATE_REQUEST_ATTRIBUTE))
            .filter(certificates -> certificates.length > 0)
            .map(certificates -> computeCertificateThumbprint(certificates[0]))
            .filter(thumbprint -> MessageDigest.isEqual(thumbprint.getBytes(StandardCharsets.UTF_8),
                certificateThumbprint.getBytes(StandardCharsets.UTF_8)))
            .isPresent();
    }

    /**
     * Add the security headers that pac4j's security logic used to add to every request it protected: no caching unless
     * the URL is a static resource, HSTS over a secure connection, and the content-type, framing and XSS protections.
     *
     * @param request  the request
     * @param response the response
     */
    public static void addSecurityHeaders(final HttpServletRequest request, final HttpServletResponse response) {
        val requestUrl = HttpRequestUtils.getFullRequestUrl(request).toLowerCase(Locale.ENGLISH);
        if (STATIC_RESOURCES.stream().noneMatch(requestUrl::endsWith)) {
            response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, max-age=0, must-revalidate");
            response.setHeader(HttpHeaders.PRAGMA, "no-cache");
            response.setHeader(HttpHeaders.EXPIRES, "0");
        }
        if (request.isSecure() || "https".equalsIgnoreCase(request.getScheme())) {
            response.setHeader("Strict-Transport-Security", "max-age=" + STRICT_TRANSPORT_SECURITY_MAX_AGE + " ; includeSubDomains");
        }
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
    }

    /**
     * The access token carried by the {@code Authorization} header of a request to a protected resource. Plain tokens use
     * the {@code Bearer} scheme of RFC 6750 and sender-constrained tokens the {@code DPoP} scheme of RFC 9449, section 7.1;
     * CAS answers a token request with {@code token_type: DPoP} whenever a proof accompanied it, so protected resources accept
     * both. Authentication schemes are case-insensitive (RFC 9110, section 11.1).
     *
     * @param request the request
     * @return the access token carried by the authorization header, if any
     */
    public static Optional<String> extractAccessTokenFromAuthorizationHeader(final HttpServletRequest request) {
        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.isBlank(authHeader)) {
            return Optional.empty();
        }
        return Stream.of(OAuth20Constants.TOKEN_TYPE_BEARER, OAuth20Constants.TOKEN_TYPE_DPOP)
            .filter(scheme -> Strings.CI.startsWith(authHeader, scheme + ' '))
            .findFirst()
            .map(scheme -> StringUtils.trimToNull(authHeader.substring(scheme.length() + 1)));
    }

    /**
     * The authentication scheme the client used to present its access token, which is the scheme a
     * {@code WWW-Authenticate} challenge has to answer in. Defaults to {@code Bearer} when the request carries no
     * recognizable scheme, since that is what a client with no token should be told to use.
     *
     * @param request the request
     * @return the authentication scheme
     */
    public static String resolveAuthorizationScheme(final HttpServletRequest request) {
        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        return Strings.CI.startsWith(authHeader, OAuth20Constants.TOKEN_TYPE_DPOP + ' ')
            ? OAuth20Constants.TOKEN_TYPE_DPOP
            : OAuth20Constants.TOKEN_TYPE_BEARER;
    }

    /**
     * The answer of a protected resource that refuses the request, with a {@code WWW-Authenticate} challenge. RFC 9110,
     * section 15.5.2 makes the challenge mandatory on a {@code 401}, and RFC 6750, section 3 says to name the error only
     * when the request actually presented credentials: a client that sent none is told which scheme to use and nothing
     * more. The challenge answers in whichever scheme the client used, so a DPoP-bound token is not told to retry as a
     * bearer token, and a DPoP-bound token sent as a bearer token is told so in the {@code Bearer} scheme (RFC 9449,
     * section 7.2). A {@code DPoP} challenge lists the algorithms accepted for DPoP proofs in its {@code algs} parameter
     * (RFC 9449, section 7.1), and an {@code insufficient_scope} challenge the scope the request needs (RFC 6750, section 3).
     *
     * @param request         the request
     * @param status          the status, {@code 401} or, for {@code insufficient_scope}, {@code 403}
     * @param error           the error code, or null when the request carried no token at all
     * @param description     the error description
     * @param scope           the scope the request needs, or null
     * @param dpopAlgorithms  the JWS algorithms accepted for DPoP proofs
     * @return the response entity
     */
    public static ResponseEntity protectedResourceErrorResponse(final HttpServletRequest request,
                                                                final HttpStatus status,
                                                                final @Nullable String error,
                                                                final @Nullable String description,
                                                                final @Nullable String scope,
                                                                final Collection<JWSAlgorithm> dpopAlgorithms) {
        val scheme = resolveAuthorizationScheme(request);
        val parameters = new ArrayList<String>();
        if (StringUtils.isNotBlank(error)) {
            parameters.add("error=\"%s\"".formatted(error));
            if (StringUtils.isNotBlank(description)) {
                parameters.add("error_description=\"%s\"".formatted(toChallengeValue(description)));
            }
            if (StringUtils.isNotBlank(scope)) {
                parameters.add("scope=\"%s\"".formatted(toChallengeValue(scope)));
            }
        }
        if (OAuth20Constants.TOKEN_TYPE_DPOP.equals(scheme)) {
            toDPoPAlgorithmsChallengeParameter(dpopAlgorithms).ifPresent(parameters::add);
        }
        val challenge = parameters.isEmpty() ? scheme : scheme + ' ' + String.join(", ", parameters);
        val response = ResponseEntity.status(status).header(HttpHeaders.WWW_AUTHENTICATE, challenge);
        return StringUtils.isBlank(error)
            ? response.build()
            : response.body(getErrorResponseBody(error, description));
    }

    /**
     * Verify the proof of possession of a request to a protected resource, when the access token is sender-constrained.
     * A DPoP proof is bound to the presented token through its {@code ath} claim and to the confirmation recorded at
     * issuance (RFC 9449, section 7.1); a token with no confirmation is an ordinary bearer token and passes straight through.
     * A missing or invalid proof is answered with {@code invalid_dpop_proof}, a proof without the server-provided nonce with
     * {@code use_dpop_nonce}, and a bound token presented as a bearer token with {@code invalid_token}; each with a
     * {@code 401} and a challenge (RFC 9449, sections 7.1, 7.2 and 9). A token that is not DPoP-bound has no key for a proof
     * to match, so presenting it under the {@code DPoP} scheme is answered with {@code invalid_token} (RFC 9449, section 7.1).
     * A token bound to the client certificate of a mutual
     * TLS client is accepted only over a connection that presents the same certificate, or answered with a {@code 401} and
     * {@code invalid_token} (RFC 8705, section 3).
     *
     * @param request              the request
     * @param response             the response
     * @param presentedAccessToken the access token exactly as the client presented it
     * @param accessToken          the access token ticket
     * @param validator            the proof of possession validator
     * @return an error response when the proof is missing or does not verify, otherwise null
     */
    public static @Nullable ResponseEntity verifyProofOfPossession(final HttpServletRequest request,
                                                                   final HttpServletResponse response,
                                                                   final String presentedAccessToken,
                                                                   final OAuth20AccessToken accessToken,
                                                                   final OAuth20ProofOfPossessionValidator validator) {
        val algorithms = validator.getAcceptedSigningAlgorithms();
        if (OAuth20Constants.TOKEN_TYPE_DPOP.equals(resolveAuthorizationScheme(request))
            && !accessToken.getAuthentication().containsAttribute(OAuth20Constants.DPOP_CONFIRMATION)) {
            LOGGER.warn("Access token presented to [{}] under the DPoP scheme is not DPoP-bound", request.getRequestURL());
            return protectedResourceErrorResponse(request, HttpStatus.UNAUTHORIZED, OAuth20Constants.INVALID_TOKEN,
                "Access token is not DPoP-bound and cannot be presented with the DPoP scheme", null, algorithms);
        }
        val boundCertificate = getBoundCertificateThumbprint(accessToken);
        if (boundCertificate.isPresent() && !isBoundCertificatePresented(request, boundCertificate.get())) {
            LOGGER.warn("Access token is bound to a client certificate that the request to [{}] does not present", request.getRequestURL());
            return protectedResourceErrorResponse(request, HttpStatus.UNAUTHORIZED, OAuth20Constants.INVALID_TOKEN,
                "Access token is bound to a client certificate that the request does not present", null, algorithms);
        }
        try {
            validator.validateProtectedResourceRequest(request, response, presentedAccessToken, accessToken);
            return null;
        } catch (final InvalidDPoPNonceException e) {
            LOGGER.info("DPoP proof of the request to [{}] carries no valid nonce; a fresh nonce is provided", request.getRequestURL());
            return useDPoPNonceResponse(algorithms);
        } catch (final DPoPBoundAccessTokenDowngradeException e) {
            LOGGER.warn(e.getMessage());
            return protectedResourceErrorResponse(request, HttpStatus.UNAUTHORIZED, OAuth20Constants.INVALID_TOKEN, e.getMessage(), null, algorithms);
        } catch (final Throwable e) {
            LoggingUtils.warn(LOGGER, e);
            return protectedResourceErrorResponse(request, HttpStatus.UNAUTHORIZED, OAuth20Constants.INVALID_DPOP_PROOF,
                StringUtils.defaultIfBlank(e.getMessage(), "DPoP proof validation failed"), null, algorithms);
        }
    }

    /**
     * Challenge parameters are quoted strings, so anything that would end the quoted string early --
     * a quote, a backslash or a control character -- is removed rather than escaped.
     *
     * @param value the value
     * @return the sanitized value
     */
    public static String toChallengeValue(final String value) {
        return value
            .chars()
            .filter(character -> character >= ' ' && character != '"' && character != '\\' && !Character.isISOControl(character))
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }

    /**
     * Verify the PKCE code verifier presented for an authorization code, per RFC 7636 section 4.6. A code issued
     * with a challenge requires a verifier that matches it exactly; a code issued without one refuses any verifier,
     * which RFC 9700 section 2.1.1 requires against downgrade attacks.
     *
     * @param code         the authorization code
     * @param codeVerifier the code verifier presented in the token request, if any
     * @return true if the code verifier is acceptable for the code
     */
    public static boolean isCodeVerifierValidFor(final OAuth20Code code, @Nullable final String codeVerifier) {
        val codeChallenge = code.getCodeChallenge();
        if (StringUtils.isBlank(codeChallenge) || StringUtils.isBlank(codeVerifier)) {
            return StringUtils.isAllBlank(codeChallenge, codeVerifier);
        }
        val method = StringUtils.defaultIfBlank(code.getCodeChallengeMethod(), "plain");
        val computedChallenge = switch (method.toUpperCase(Locale.ENGLISH)) {
            case "S256" -> EncodingUtils.encodeUrlSafeBase64(DigestUtils.rawDigestSha256(codeVerifier));
            case "PLAIN" -> codeVerifier;
            default -> null;
        };
        return computedChallenge != null && MessageDigest.isEqual(
            computedChallenge.getBytes(StandardCharsets.UTF_8), codeChallenge.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Authorization details a token was granted with, per RFC 9396: those an authorization code carries, or a
     * refresh token carries from the code it was issued for. Other tokens contribute none.
     *
     * @param token the token being exchanged, if any
     * @return the authorization details, never null
     */
    public static List<? extends Serializable> getAuthorizationDetails(@Nullable final Ticket token) {
        final List<? extends Serializable> authorizationDetails = switch (token) {
            case final OAuth20Code code -> code.getAuthorizationDetails();
            case final OAuth20RefreshToken refreshToken -> refreshToken.getAuthorizationDetails();
            case null, default -> null;
        };
        return authorizationDetails == null ? new ArrayList<>() : authorizationDetails;
    }

    /**
     * Authorization details as untyped JSON for a compact ticket, or an empty string when there are none.
     *
     * @param authorizationDetails the authorization details
     * @return the compact value
     */
    public static String toCompactAuthorizationDetails(@Nullable final List<? extends Serializable> authorizationDetails) {
        return authorizationDetails == null || authorizationDetails.isEmpty()
            ? StringUtils.EMPTY
            : UNTYPED_MAPPER.writeValueAsString(authorizationDetails);
    }

    /**
     * Authorization details read back from a compact ticket, each as a map of its JSON members.
     *
     * @param value the compact value
     * @return the authorization details, never null
     */
    public static List<? extends Serializable> fromCompactAuthorizationDetails(@Nullable final String value) {
        return StringUtils.isBlank(value)
            ? new ArrayList<>()
            : UNTYPED_MAPPER.readValue(value, new TypeReference<ArrayList<LinkedHashMap<String, Object>>>() {
            });
    }

    /**
     * Verify that every requested resource indicator is one the service allows (RFC 8707, section 2).
     *
     * @param requestParameterResolver the request parameter resolver
     * @param request                  the request
     * @param registeredService        the registered service
     * @throws OAuth20InvalidTargetException when a requested resource is invalid or not allowed
     */
    public static void verifyRequestedResources(final OAuth20RequestParameterResolver requestParameterResolver,
                                                final HttpServletRequest request,
                                                final OAuthRegisteredService registeredService) {
        if (!requestParameterResolver.isAuthorizedResourceForService(request, registeredService)) {
            throw new OAuth20InvalidTargetException("Requested resource is invalid or not authorized for client %s"
                .formatted(registeredService.getClientId()));
        }
    }

    /**
     * Resources requested at the token endpoint for a grant that already carries resources, such as a code or a
     * refresh token (RFC 8707, section 2.2). Without a request, the token keeps the resources of the grant. Otherwise,
     * the requested resources must be among those of the grant, or match the resources the service may request when the
     * grant carries none, and the token is issued for the requested resources only.
     *
     * @param requestedResources the requested resources
     * @param grant              the code or token presented as the grant
     * @param allowedResources   the resources the service may request, as regular expressions
     * @return the resources of the token to issue
     * @throws OAuth20InvalidTargetException when a requested resource is not part of the grant
     */
    public static Set<String> narrowRequestedResources(final Set<String> requestedResources,
                                                       final OAuth20Token grant,
                                                       final Collection<String> allowedResources) {
        if (requestedResources.isEmpty()) {
            return new LinkedHashSet<>(grant.getResources());
        }
        val permitted = grant.getResources().isEmpty()
            ? RegisteredServiceOAuthResourceMatcher.areResourcesAllowed(allowedResources, requestedResources)
            : grant.getResources().containsAll(requestedResources);
        if (!permitted) {
            LOGGER.debug("Requested resources [{}] exceed the resources granted to [{}]", requestedResources, grant.getId());
            throw new OAuth20InvalidTargetException("Requested resource is not part of the grant %s".formatted(grant.getId()));
        }
        return new LinkedHashSet<>(requestedResources);
    }

    /**
     * Determine the audience of a token, shared by JWT access tokens and token introspection. A token whose audience
     * was fixed when it was issued, as by a token exchange, has that audience only. Otherwise, the audience is the
     * service audience, or the client id when the service defines none, along with the resources granted to the token.
     *
     * @param registeredService the registered service
     * @param token             the token
     * @return the audience
     */
    public static Set<String> determineAudience(final OAuthRegisteredService registeredService, final OAuth20Token token) {
        if (!token.getAudience().isEmpty()) {
            return new LinkedHashSet<>(token.getAudience());
        }
        val audience = new LinkedHashSet<String>();
        if (registeredService.getAudience().isEmpty()) {
            audience.add(token.getClientId());
        } else {
            audience.addAll(registeredService.getAudience());
        }
        audience.addAll(token.getResources());
        return audience;
    }
}
