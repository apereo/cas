package org.apereo.cas.support.oauth.web;

import module java.base;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link OAuth20RequestParameterResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public interface OAuth20RequestParameterResolver {
    /**
     * Default bean name.
     */
    String BEAN_NAME = "oauthRequestParameterResolver";
    
    /**
     * Logger instance.
     */
    Logger LOGGER = LoggerFactory.getLogger(OAuth20RequestParameterResolver.class);

    /**
     * Determines whether the grant type is authorized for the service.
     *
     * @param grantType         the grant type
     * @param registeredService the registered service
     * @return true/false
     */
    static boolean isAuthorizedGrantTypeForService(final String grantType,
                                                   final OAuthRegisteredService registeredService) {
        return isAuthorizedGrantTypeForService(grantType, registeredService, false);
    }

    /**
     * Determines whether the grant type is authorized for the service.
     *
     * @param grantType         the grant type
     * @param registeredService the registered service
     * @param rejectUndefined   the reject undefined
     * @return true /false
     */
    static boolean isAuthorizedGrantTypeForService(final String grantType,
                                                   final OAuthRegisteredService registeredService,
                                                   final boolean rejectUndefined) {
        if (registeredService.getSupportedGrantTypes() != null && !registeredService.getSupportedGrantTypes().isEmpty()) {
            LOGGER.debug("Checking grant type [{}] against supported grant types [{}]", grantType, registeredService.getSupportedGrantTypes());
            return registeredService.getSupportedGrantTypes().stream().anyMatch(s -> s.equalsIgnoreCase(grantType));
        }

        LOGGER.warn("Registered service [{}] does not define any authorized/supported grant types. "
            + "It is STRONGLY recommended that you authorize and assign grant types to the service definition. "
            + "While just a warning for now, this behavior will be enforced by CAS in future versions.", registeredService.getName());
        return !rejectUndefined;
    }

    /**
     * Is authorized grant type for service.
     *
     * @param request           the request
     * @param registeredService the registered service
     * @return true/false
     */
    boolean isAuthorizedGrantTypeForService(HttpServletRequest request,
                                            OAuthRegisteredService registeredService);

    /**
     * Resolve response type.
     *
     * @param request the request
     * @return the response types
     */
    OAuth20ResponseTypes resolveResponseType(HttpServletRequest request);

    /**
     * Resolve grant type.
     *
     * @param request the request
     * @return the grant types
     */
    OAuth20GrantTypes resolveGrantType(HttpServletRequest request);

    /**
     * Resolve response mode.
     *
     * @param request the request
     * @return response mode types
     */
    OAuth20ResponseModeTypes resolveResponseModeType(HttpServletRequest request);

    /**
     * Resolve jwt request parameter.
     *
     * @param <T>        the type parameter
     * @param jwtRequest the jwt request
     * @param service    the service
     * @param name       the name
     * @param clazz      the clazz
     * @return the type
     * @throws Exception the exception
     */
    <T> T resolveJwtRequestParameter(String jwtRequest, RegisteredService service,
                                     String name, Class<T> clazz)
            throws Exception;

    /**
     * Resolve jwt request.
     *
     * @param <T>        the type parameter
     * @param request    the request
     * @param jwtRequest the jwt request
     * @param name       the name
     * @param clazz      the clazz
     * @return the type
     */
    <T> T resolveJwtRequestParameter(HttpServletRequest request, String jwtRequest,
                                     String name, Class<T> clazz);

    /**
     * Resolve several parameters from the given request object, unpacking and verifying it once.
     * Parameters the request object does not carry are left out of the result.
     *
     * @param <T>        the type parameter
     * @param jwtRequest the jwt request
     * @param service    the service
     * @param names      the parameter names
     * @param clazz      the clazz
     * @return the parameter values by name
     * @throws Exception the exception
     */
    <T> Map<String, T> resolveJwtRequestParameters(String jwtRequest, RegisteredService service,
                                                   Collection<String> names, Class<T> clazz)
            throws Exception;

    /**
     * Resolve request parameters map.
     *
     * @param attributes the attributes
     * @param request    the request
     * @return the map
     */
    Map<String, Set<String>> resolveRequestParameters(Collection<String> attributes,
                                                      HttpServletRequest request);

    /**
     * Resolve request parameter.
     *
     * @param request the request
     * @param name    the name
     * @return the optional
     */
    Set<String> resolveRequestParameters(HttpServletRequest request, String name);

    /**
     * Resolve request parameter.
     *
     * @param request the request
     * @param name    the name
     * @return the optional
     */
    Optional<String> resolveRequestParameter(HttpServletRequest request, String name);

    /**
     * Resolve request parameter.
     *
     * @param <T>     the type parameter
     * @param request the request
     * @param name    the name
     * @param clazz   the clazz
     * @return the optional
     */
    <T> Optional<T> resolveRequestParameter(HttpServletRequest request, String name, Class<T> clazz);

    /**
     * Resolve requested scopes.
     *
     * @param request the request
     * @return the collection
     */
    Collection<String> resolveRequestedScopes(HttpServletRequest request);

    /**
     * Resolve requested resource indicators.
     *
     * @param request the request
     * @return the resources
     */
    Set<String> resolveRequestedResources(HttpServletRequest request);

    /**
     * Checks whether every requested resource is an absolute URI without a fragment that the service allows.
     *
     * @param request           the request
     * @param registeredService the registered service
     * @return true/false
     */
    boolean isAuthorizedResourceForService(HttpServletRequest request, OAuthRegisteredService registeredService);

    /**
     * Resolve the resources, as regular expressions, a service may request: the allowed resources of the service,
     * along with those CAS allows for every client.
     *
     * @param registeredService the registered service
     * @return the allowed resources
     */
    Set<String> resolveAllowedResources(OAuthRegisteredService registeredService);

    /**
     * Is authorized response type for service.
     *
     * @param request           the request
     * @param registeredService the registered service
     * @return true/false
     */
    boolean isAuthorizedResponseTypeForService(HttpServletRequest request, OAuthRegisteredService registeredService);

    /**
     * Resolve client id and client secret pair.
     *
     * @param request the request
     * @return the pair
     */
    Pair<String, String> resolveClientIdAndClientSecret(HttpServletRequest request);

    /**
     * Resolve request scopes set.
     *
     * @param request the request
     * @return the set
     */
    Set<String> resolveRequestScopes(HttpServletRequest request);

    /**
     * Resolve request claims map.
     *
     * @param request the request
     * @return the map
     * @throws Exception the exception
     */
    Map<String, Map<String, Object>> resolveRequestClaims(HttpServletRequest request) throws Exception;

    /**
     * Resolve user info request claims set.
     *
     * @param request the request
     * @return the set
     * @throws Exception the exception
     */
    Set<String> resolveUserInfoRequestClaims(HttpServletRequest request) throws Exception;

    /**
     * Resolve prompt parameter set.
     *
     * @param request the request
     * @return the set
     */
    Set<String> resolveRequestedPromptValues(HttpServletRequest request);

    /**
     * Resolve prompt values.
     *
     * @param url the url
     * @return the set
     */
    Set<String> resolveSupportedPromptValues(String url);

    /**
     * Resolve prompt values.
     *
     * @param request the request
     * @return the set
     */
    default Set<String> resolveSupportedPromptValues(final HttpServletRequest request) {
        return resolveSupportedPromptValues(HttpRequestUtils.getFullRequestUrl(request));
    }

    /**
     * Is the provided parameter name on the query string.
     *
     * @param request the request
     * @param name    the parameter name
     * @return whether the parameter name is on the query string
     */
    boolean isParameterOnQueryString(HttpServletRequest request, String name);
}
