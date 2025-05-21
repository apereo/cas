package org.apereo.cas.support.oauth.web;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apache.commons.lang3.tuple.Pair;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.WebContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
     * Is authorized grant type for service?
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
     * Is authorized grant type for service?
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
     * @param context           the context
     * @param registeredService the registered service
     * @return true/false
     */
    boolean isAuthorizedGrantTypeForService(WebContext context,
                                            OAuthRegisteredService registeredService);

    /**
     * Resolve response type.
     *
     * @param context the context
     * @return the response types
     */
    OAuth20ResponseTypes resolveResponseType(WebContext context);

    /**
     * Resolve grant type.
     *
     * @param context the context
     * @return the grant types
     */
    OAuth20GrantTypes resolveGrantType(WebContext context);

    /**
     * Resolve response mode.
     *
     * @param context the context
     * @return response mode types
     */
    OAuth20ResponseModeTypes resolveResponseModeType(WebContext context);

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
                                     String name, Class<T> clazz) throws Exception;

    /**
     * Resolve jwt request.
     *
     * @param <T>        the type parameter
     * @param context    the context
     * @param jwtRequest the jwt request
     * @param name       the name
     * @param clazz      the clazz
     * @return the type
     */
    <T> T resolveJwtRequestParameter(WebContext context, String jwtRequest,
                                     String name, Class<T> clazz);

    /**
     * Resolve request parameters map.
     *
     * @param attributes the attributes
     * @param context    the context
     * @return the map
     */
    Map<String, Set<String>> resolveRequestParameters(Collection<String> attributes,
                                                      WebContext context);

    /**
     * Resolve request parameter.
     *
     * @param context the context
     * @param name    the name
     * @return the optional
     */
    Set<String> resolveRequestParameters(WebContext context, String name);

    /**
     * Resolve request parameter.
     *
     * @param context the context
     * @param name    the name
     * @return the optional
     */
    Optional<String> resolveRequestParameter(WebContext context, String name);

    /**
     * Resolve request parameter.
     *
     * @param <T>     the type parameter
     * @param context the context
     * @param name    the name
     * @param clazz   the clazz
     * @return the optional
     */
    <T> Optional<T> resolveRequestParameter(WebContext context, String name, Class<T> clazz);

    /**
     * Resolve requested scopes.
     *
     * @param context the context
     * @return the collection
     */
    Collection<String> resolveRequestedScopes(WebContext context);

    /**
     * Is authorized response type for service.
     *
     * @param context           the context
     * @param registeredService the registered service
     * @return true/false
     */
    boolean isAuthorizedResponseTypeForService(WebContext context, OAuthRegisteredService registeredService);

    /**
     * Resolve client id and client secret pair.
     *
     * @param callContext the call context
     * @return the pair
     */
    Pair<String, String> resolveClientIdAndClientSecret(CallContext callContext);

    /**
     * Resolve request scopes set.
     *
     * @param context the context
     * @return the set
     */
    Set<String> resolveRequestScopes(WebContext context);

    /**
     * Resolve request claims map.
     *
     * @param context the context
     * @return the map
     * @throws Exception the exception
     */
    Map<String, Map<String, Object>> resolveRequestClaims(WebContext context) throws Exception;

    /**
     * Resolve user info request claims set.
     *
     * @param context the context
     * @return the set
     * @throws Exception the exception
     */
    Set<String> resolveUserInfoRequestClaims(WebContext context) throws Exception;

    /**
     * Resolve prompt parameter set.
     *
     * @param context the context
     * @return the set
     */
    Set<String> resolveRequestedPromptValues(WebContext context);

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
     * @param context the context
     * @return the set
     */
    default Set<String> resolveSupportedPromptValues(final WebContext context) {
        return resolveSupportedPromptValues(context.getFullRequestURL());
    }

    /**
     * Is the provided parameter name on the query string.
     *
     * @param context the web context
     * @param name    the parameter name
     * @return whether the parameter name is on the query string
     */
    boolean isParameterOnQueryString(WebContext context, String name);
}
