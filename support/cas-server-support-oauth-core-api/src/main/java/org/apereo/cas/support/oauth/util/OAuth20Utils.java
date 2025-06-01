package org.apereo.cas.support.oauth.util;

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
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGeneratedResult;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.flow.CasWebflowConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.client.RedirectURIValidator;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .singleArrayElementUnwrapped(true).build().toObjectMapper();

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
                                          final String error, final String description) {
        val model = getErrorResponseBody(error, description);
        val mv = new ModelAndView(new MappingJackson2JsonView(MAPPER), model);
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
    public static Map<String, Object> getErrorResponseBody(final String error, final String description) {
        val model = CollectionUtils.<String, Object>wrap(OAuth20Constants.ERROR, error);
        if (StringUtils.isNotBlank(description)) {
            model.put(OAuth20Constants.ERROR_DESCRIPTION, description);
        }
        return model;
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
    public static <T extends OAuthRegisteredService> T getRegisteredOAuthServiceByClientId(final ServicesManager servicesManager,
                                                                                           final String clientId,
                                                                                           final Class<T> clazz) {
        return FunctionUtils.doIfNotBlank(clientId,
            () -> {
                val query = RegisteredServiceQuery.of(OAuthRegisteredService.class, "clientId", clientId).withIncludeAssignableTypes(true);
                return servicesManager.findServicesBy(query).findFirst().map(clazz::cast).orElse(null);
            },
            () -> null);
    }

    /**
     * Locate the requested instance of {@link OAuthRegisteredService} by the given clientId.
     *
     * @param servicesManager the service registry DAO instance.
     * @param clientId        the client id by which the {@link OAuthRegisteredService} is to be located.
     * @return null, or the located {@link OAuthRegisteredService} instance in the service registry.
     */
    public static OAuthRegisteredService getRegisteredOAuthServiceByClientId(final ServicesManager servicesManager,
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
    public static OAuthRegisteredService getRegisteredOAuthServiceByRedirectUri(final ServicesManager servicesManager,
                                                                                final String redirectUri) {
        validateRedirectUri(redirectUri);
        return FunctionUtils.doIfNotBlank(redirectUri,
            () -> getRegisteredOAuthServiceByPredicate(servicesManager, service -> service.matches(redirectUri)),
            () -> null);
    }

    private static OAuthRegisteredService getRegisteredOAuthServiceByPredicate(final ServicesManager servicesManager,
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
        return FunctionUtils.doUnchecked(() -> MAPPER.writeValueAsString(value));
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
     * Is response mode type expected?
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
     * @param context the context
     * @return the service request header if any
     */
    public static String getServiceRequestHeaderIfAny(final WebContext context) {
        return context.getRequestHeader(CasProtocolConstants.PARAMETER_SERVICE)
            .or(() -> context.getRequestHeader("X-".concat(CasProtocolConstants.PARAMETER_SERVICE)))
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
        val matchingStrategy = Optional.of(registeredService).map(RegisteredService::getMatchingStrategy).orElse(null);
        validateRedirectUri(redirectUri);
        if (matchingStrategy == null || !matchingStrategy.matches(registeredService, redirectUri)) {
            LOGGER.error("Unsupported [{}]: [{}] does not match what is defined for registered service: [{}]. "
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
    public static String getClientIdFromAuthenticatedProfile(final UserProfile profile) {
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
     * Gets authenticated user profile.
     *
     * @param context      the context
     * @param sessionStore the session store
     * @return the authenticated user profile
     */
    public UserProfile getAuthenticatedUserProfile(final WebContext context, final SessionStore sessionStore) {
        val manager = new ProfileManager(context, sessionStore);
        val profile = manager.getProfile();
        return profile.orElseThrow(() -> new IllegalArgumentException("Unable to determine the user profile from the context"));
    }

    /**
     * Is the registered service need authentication?
     *
     * @param registeredService the registered service
     * @return whether the service need authentication
     */
    public boolean doesServiceNeedAuthentication(final OAuthRegisteredService registeredService) {
        return StringUtils.isNotBlank(registeredService.getClientSecret());
    }

    /**
     * Validate redirect uri.
     *
     * @param redirectUri the redirect uri
     */
    public void validateRedirectUri(final String redirectUri) {
        if (StringUtils.isNotBlank(redirectUri)) {
            RedirectURIValidator.ensureLegal(URI.create(redirectUri));
        }
    }

    /**
     * Is access token request?.
     *
     * @param webContext the web context
     * @return true or false
     */
    public static boolean isAccessTokenRequest(final WebContext webContext) {
        return (Boolean) webContext.getRequestAttribute(OAuth20Constants.REQUEST_ATTRIBUTE_ACCESS_TOKEN_REQUEST).orElse(false);
    }

    /**
     * Is token authentication method supported for service.
     *
     * @param callContext          the call context
     * @param registeredService    the registered service
     * @param authenticationMethod the authentication method
     * @return true/false
     */
    public static boolean isTokenAuthenticationMethodSupportedFor(final CallContext callContext,
                                                                  final OAuthRegisteredService registeredService,
                                                                  final OAuth20ClientAuthenticationMethods... authenticationMethod) {
        return !OAuth20Utils.isAccessTokenRequest(callContext.webContext())
            || StringUtils.isBlank(registeredService.getTokenEndpointAuthenticationMethod())
            || Arrays.stream(authenticationMethod).anyMatch(method -> StringUtils.equalsIgnoreCase(registeredService.getTokenEndpointAuthenticationMethod(), method.getType()));
    }

    /**
     * Find stateless ticket validation result.
     *
     * @param manager the manager
     * @return the ticket validation result
     */
    public static Boolean isStatelessAuthentication(final ProfileManager manager) {
        return manager
            .getProfile()
            .stream()
            .map(OAuth20Utils::isStatelessAuthentication)
            .findFirst()
            .orElse(Boolean.FALSE);
    }

    /**
     * Find stateless ticket validation result.
     *
     * @param profile the profile
     * @return the ticket validation result
     */
    public static Boolean isStatelessAuthentication(final UserProfile profile) {
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
}
