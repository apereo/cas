package org.apereo.cas.support.oauth.util;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.flow.CasWebflowConstants;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hjson.JsonValue;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.extractor.BasicAuthExtractor;
import org.pac4j.core.profile.UserProfile;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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
        val model = CollectionUtils.wrap(OAuth20Constants.ERROR, error);
        val mv = new ModelAndView(new MappingJackson2JsonView(MAPPER), (Map) model);
        mv.setStatus(HttpStatus.BAD_REQUEST);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return mv;
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
        if (StringUtils.isBlank(clientId)) {
            return null;
        }
        return getRegisteredOAuthServiceByPredicate(servicesManager, s -> s.getClientId().equalsIgnoreCase(clientId));
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
        if (StringUtils.isBlank(redirectUri)) {
            return null;
        }
        return getRegisteredOAuthServiceByPredicate(servicesManager, s -> s.matches(redirectUri));
    }

    private static OAuthRegisteredService getRegisteredOAuthServiceByPredicate(final ServicesManager servicesManager,
                                                                               final Predicate<OAuthRegisteredService> predicate) {
        val services = servicesManager.getAllServicesOfType(OAuthRegisteredService.class);
        return services.stream()
            .filter(predicate)
            .findFirst()
            .orElse(null);
    }

    /**
     * Gets attributes.
     *
     * @param attributes the attributes
     * @param context    the context
     * @return the attributes
     */
    public static Map<String, Object> getRequestParameters(final Collection<String> attributes, final WebContext context) {
        return attributes
            .stream()
            .map(name -> {
                val values = getRequestParameter(context, name)
                    .map(value -> Arrays.stream(value.split(" ")).collect(Collectors.toSet()))
                    .orElse(Set.of());
                return Pair.of(name, values);
            })
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    /**
     * Gets authorization parameter.
     *
     * @param context the context
     * @param name    the name
     * @return the authorization parameter
     */
    public static Optional<String> getRequestParameter(final WebContext context,
                                                       final String name) {
        return getRequestParameter(context, name, String.class);
    }

    /**
     * Gets request parameter.
     *
     * @param <T>     the type parameter
     * @param context the context
     * @param name    the name
     * @param clazz   the clazz
     * @return the request parameter
     */
    public static <T> Optional<T> getRequestParameter(final WebContext context,
                                                      final String name,
                                                      final Class<T> clazz) {
        return context.getRequestParameter(OAuth20Constants.REQUEST)
            .map(Unchecked.function(jwtRequest -> getJwtRequestParameter(jwtRequest, name, clazz)))
            .or(() -> {
                val values = context.getRequestParameters().get(name);
                if (values != null && values.length > 0) {
                    if (clazz.isArray()) {
                        return Optional.<T>of(clazz.cast(values));
                    }
                    if (Collection.class.isAssignableFrom(clazz)) {
                        return Optional.<T>of(clazz.cast(CollectionUtils.wrapArrayList(values)));
                    }
                    return Optional.<T>of(clazz.cast(values[0]));
                }
                return Optional.<T>empty();
            });
    }

    /**
     * Gets jwt request parameter.
     *
     * @param <T>        the type parameter
     * @param jwtRequest the jwt request
     * @param name       the name
     * @param clazz      the clazz
     * @return the jwt request parameter
     * @throws Exception the exception
     */
    public static <T> T getJwtRequestParameter(final String jwtRequest, final String name,
                                               final Class<T> clazz) throws Exception {
        val jwt = JwtBuilder.parse(jwtRequest);
        if (clazz.isArray()) {
            return clazz.cast(jwt.getStringArrayClaim(name));
        }
        if (Collection.class.isAssignableFrom(clazz)) {
            return clazz.cast(jwt.getStringListClaim(name));
        }
        return clazz.cast(jwt.getStringClaim(name));
    }

    /**
     * Gets requested scopes.
     *
     * @param context the context
     * @return the requested scopes
     */
    public static Collection<String> getRequestedScopes(final WebContext context) {
        val map = getRequestParameters(CollectionUtils.wrap(OAuth20Constants.SCOPE), context);
        if (map == null || map.isEmpty()) {
            return new ArrayList<>(0);
        }
        return (Collection<String>) map.get(OAuth20Constants.SCOPE);
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
        val ex = new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
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
    @SneakyThrows
    public static String toJson(final Object value) {
        return MAPPER.writeValueAsString(value);
    }

    /**
     * Is response mode type form post?
     *
     * @param registeredService the registered service
     * @param responseType      the response type
     * @return true/false
     */
    public static boolean isResponseModeTypeFormPost(final OAuthRegisteredService registeredService, final OAuth20ResponseModeTypes responseType) {
        return responseType == OAuth20ResponseModeTypes.FORM_POST
            || (registeredService != null && StringUtils.equalsIgnoreCase("post", registeredService.getResponseType()));
    }

    /**
     * Gets response type.
     *
     * @param context the context
     * @return the response type
     */
    public static OAuth20ResponseTypes getResponseType(final WebContext context) {
        val responseType = getRequestParameter(context, OAuth20Constants.RESPONSE_TYPE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val type = Arrays.stream(OAuth20ResponseTypes.values())
            .filter(t -> t.getType().equalsIgnoreCase(responseType))
            .findFirst()
            .orElse(OAuth20ResponseTypes.CODE);
        LOGGER.debug("OAuth response type is [{}]", type);
        return type;
    }

    /**
     * Gets response mode type.
     *
     * @param context the context
     * @return the response type
     */
    public static OAuth20ResponseModeTypes getResponseModeType(final WebContext context) {
        val responseType = getRequestParameter(context, OAuth20Constants.RESPONSE_MODE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val type = Arrays.stream(OAuth20ResponseModeTypes.values())
            .filter(t -> t.getType().equalsIgnoreCase(responseType))
            .findFirst()
            .orElse(OAuth20ResponseModeTypes.NONE);
        LOGGER.debug("OAuth response type is [{}]", type);
        return type;
    }

    /**
     * Check the grant type against an expected grant type.
     *
     * @param type         the given grant type
     * @param expectedType the expected grant type
     * @return whether the grant type is the expected one
     */
    public static boolean isGrantType(final String type, final OAuth20GrantTypes expectedType) {
        return expectedType.name().equalsIgnoreCase(type);
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
     * Is authorized response type for service?
     *
     * @param context           the context
     * @param registeredService the registered service
     * @return true/false
     */
    public static boolean isAuthorizedResponseTypeForService(final WebContext context, final OAuthRegisteredService registeredService) {
        if (registeredService.getSupportedResponseTypes() != null && !registeredService.getSupportedResponseTypes().isEmpty()) {
            val responseType = getRequestParameter(context, OAuth20Constants.RESPONSE_TYPE).map(String::valueOf).orElse(StringUtils.EMPTY);
            if (registeredService.getSupportedResponseTypes().stream().anyMatch(s -> s.equalsIgnoreCase(responseType))) {
                return true;
            }
            LOGGER.warn("Response type not authorized for service: [{}] not listed in supported response types: [{}]",
                responseType, registeredService.getSupportedResponseTypes());
            return false;
        }

        LOGGER.warn("Registered service [{}] does not define any authorized/supported response types. "
            + "It is STRONGLY recommended that you authorize and assign response types to the service definition. "
            + "While just a warning for now, this behavior will be enforced by CAS in future versions.", registeredService.getName());
        return true;
    }

    /**
     * Is authorized grant type for service?
     *
     * @param grantType         the grant type
     * @param registeredService the registered service
     * @return true/false
     */
    public static boolean isAuthorizedGrantTypeForService(final String grantType,
                                                          final OAuthRegisteredService registeredService) {
        if (registeredService.getSupportedGrantTypes() != null && !registeredService.getSupportedGrantTypes().isEmpty()) {
            LOGGER.debug("Checking grant type [{}] against supported grant types [{}]", grantType, registeredService.getSupportedGrantTypes());
            return registeredService.getSupportedGrantTypes().stream().anyMatch(s -> s.equalsIgnoreCase(grantType));
        }

        LOGGER.warn("Registered service [{}] does not define any authorized/supported grant types. "
            + "It is STRONGLY recommended that you authorize and assign grant types to the service definition. "
            + "While just a warning for now, this behavior will be enforced by CAS in future versions.", registeredService.getName());
        return true;
    }

    /**
     * Is authorized grant type for service?
     *
     * @param context           the context
     * @param registeredService the registered service
     * @return true/false
     */
    public static boolean isAuthorizedGrantTypeForService(final WebContext context, final OAuthRegisteredService registeredService) {
        val grantType = getRequestParameter(context, OAuth20Constants.GRANT_TYPE).map(String::valueOf).orElse(StringUtils.EMPTY);
        return isAuthorizedGrantTypeForService(grantType, registeredService);
    }

    /**
     * Parse request scopes set.
     *
     * @param context the context
     * @return the set
     */
    public static Set<String> parseRequestScopes(final WebContext context) {
        val parameterValues = getRequestParameter(context, OAuth20Constants.SCOPE);
        if (parameterValues.isEmpty()) {
            return new HashSet<>(0);
        }
        return CollectionUtils.wrapSet(parameterValues.get().split(" "));
    }

    /**
     * Gets service request header if any.
     *
     * @param context the context
     * @return the service request header if any
     */
    public static String getServiceRequestHeaderIfAny(final HttpServletRequest context) {
        if (context == null) {
            return null;
        }
        var id = context.getHeader(CasProtocolConstants.PARAMETER_SERVICE);
        if (StringUtils.isBlank(id)) {
            id = context.getHeader("X-".concat(CasProtocolConstants.PARAMETER_SERVICE));
        }
        return id;
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
        val matchingStrategy = registeredService != null ? registeredService.getMatchingStrategy() : null;
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
     * Check the client secret.
     *
     * @param registeredService the registered service
     * @param clientSecret      the client secret
     * @param cipherExecutor    the cipher executor
     * @return whether the secret is valid
     */
    public static boolean checkClientSecret(final OAuthRegisteredService registeredService, final String clientSecret,
                                            final CipherExecutor<Serializable, String> cipherExecutor) {
        LOGGER.debug("Found: [{}] in secret check", registeredService);
        var definedSecret = registeredService.getClientSecret();
        if (StringUtils.isBlank(definedSecret)) {
            LOGGER.debug("The client secret is not defined for the registered service [{}]", registeredService.getName());
            return true;
        }
        definedSecret = cipherExecutor.decode(definedSecret, new Object[]{registeredService});
        if (!StringUtils.equals(definedSecret, clientSecret)) {
            LOGGER.error("Wrong client secret for service: [{}]", registeredService.getServiceId());
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
            return CollectionUtils.toCollection(attribute, ArrayList.class).get(0).toString();
        }
        return null;
    }

    /**
     * Parse request claims map.
     *
     * @param context the context
     * @return the map
     * @throws Exception the exception
     */
    public static Map<String, Map<String, Object>> parseRequestClaims(final WebContext context) throws Exception {
        val claims = getRequestParameter(context, OAuth20Constants.CLAIMS).map(String::valueOf).orElse(StringUtils.EMPTY);
        if (StringUtils.isBlank(claims)) {
            return new HashMap<>(0);
        }
        return MAPPER.readValue(JsonValue.readHjson(claims).toString(), Map.class);
    }

    /**
     * Parse user info request claims set.
     *
     * @param token the token
     * @return the set
     */
    public static Set<String> parseUserInfoRequestClaims(final OAuth20Token token) {
        return token.getClaims().getOrDefault("userinfo", new HashMap<>(0)).keySet();
    }

    /**
     * Parse user info request claims set.
     *
     * @param context the context
     * @return the set
     * @throws Exception the exception
     */
    public static Set<String> parseUserInfoRequestClaims(final WebContext context) throws Exception {
        val requestedClaims = parseRequestClaims(context);
        return requestedClaims.getOrDefault(OAuth20Constants.CLAIMS_USERINFO, new HashMap<>(0)).keySet();
    }

    /**
     * Gets client id and client secret.
     *
     * @param webContext   the web context
     * @param sessionStore the session store
     * @return the client id and client secret
     */
    public static Pair<String, String> getClientIdAndClientSecret(final WebContext webContext, final SessionStore sessionStore) {
        val extractor = new BasicAuthExtractor();
        val upcResult = extractor.extract(webContext, sessionStore);
        if (upcResult.isPresent()) {
            val upc = (UsernamePasswordCredentials) upcResult.get();
            return Pair.of(upc.getUsername(), upc.getPassword());
        }
        val clientId = getRequestParameter(webContext, OAuth20Constants.CLIENT_ID)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val clientSecret = getRequestParameter(webContext, OAuth20Constants.CLIENT_SECRET)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        return Pair.of(clientId, clientSecret);
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
}
