package org.apereo.cas.support.oauth.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpStatus;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.pac4j.core.context.J2EContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apereo.cas.support.oauth.OAuth20Constants.BASE_OAUTH20_URL;

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
    private static final ObjectWriter WRITER = new ObjectMapper().findAndRegisterModules().writer().withDefaultPrettyPrinter();

    /**
     * Write to the output this error text and return a null view.
     *
     * @param response http response
     * @param error    error message
     * @return a null view
     */
    public static ModelAndView writeTextError(final HttpServletResponse response, final String error) {
        return OAuth20Utils.writeText(response, OAuth20Constants.ERROR + '=' + error, HttpStatus.SC_BAD_REQUEST);
    }

    /**
     * Write to the output the text and return a null view.
     *
     * @param response http response
     * @param text     output text
     * @param status   status code
     * @return a null view
     */
    public static ModelAndView writeText(final HttpServletResponse response, final String text, final int status) {
        try (PrintWriter printWriter = response.getWriter()) {
            response.setStatus(status);
            printWriter.print(text);
        } catch (final IOException e) {
            LOGGER.error("Failed to write to response", e);
        }
        return null;
    }

    /**
     * Redirect to model and view.
     *
     * @param view the view
     * @return the model and view
     */
    public static ModelAndView redirectTo(final View view) {
        return new ModelAndView(view);
    }

    /**
     * Locate the requested instance of {@link OAuthRegisteredService} by the given clientId.
     *
     * @param servicesManager the service registry DAO instance.
     * @param clientId        the client id by which the {@link OAuthRegisteredService} is to be located.
     * @return null, or the located {@link OAuthRegisteredService} instance in the service registry.
     */
    public static OAuthRegisteredService getRegisteredOAuthServiceByClientId(final ServicesManager servicesManager, final String clientId) {
        return getRegisteredOAuthServiceByPredicate(servicesManager, s -> s.getClientId().equals(clientId));
    }

    /**
     * Gets registered oauth service by redirect uri.
     *
     * @param servicesManager the services manager
     * @param redirectUri     the redirect uri
     * @return the registered o auth service by redirect uri
     */
    public static OAuthRegisteredService getRegisteredOAuthServiceByRedirectUri(final ServicesManager servicesManager, final String redirectUri) {
        return getRegisteredOAuthServiceByPredicate(servicesManager, s -> s.matches(redirectUri));
    }

    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    private static OAuthRegisteredService getRegisteredOAuthServiceByPredicate(final ServicesManager servicesManager,
                                                                               final Predicate<OAuthRegisteredService> predicate) {
        final Collection<RegisteredService> services = servicesManager.getAllServices();
        return services.stream()
            .filter(OAuthRegisteredService.class::isInstance)
            .map(OAuthRegisteredService.class::cast)
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
    public static Map<String, Object> getRequestParameters(final Collection<String> attributes, final HttpServletRequest context) {
        return attributes.stream()
            .filter(a -> StringUtils.isNotBlank(context.getParameter(a)))
            .map(m -> {
                final String[] values = context.getParameterValues(m);
                final Collection<String> valuesSet = new LinkedHashSet<>();
                if (values != null && values.length > 0) {
                    Arrays.stream(values).forEach(v -> valuesSet.addAll(Arrays.stream(v.split(" ")).collect(Collectors.toSet())));
                }
                return Pair.of(m, valuesSet);
            })
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    /**
     * Gets requested scopes.
     *
     * @param context the context
     * @return the requested scopes
     */
    public static Collection<String> getRequestedScopes(final J2EContext context) {
        return getRequestedScopes(context.getRequest());
    }

    /**
     * Gets requested scopes.
     *
     * @param context the context
     * @return the requested scopes
     */
    public static Collection<String> getRequestedScopes(final HttpServletRequest context) {
        final Map<String, Object> map = getRequestParameters(CollectionUtils.wrap(OAuth20Constants.SCOPE), context);
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
        return produceErrorView(new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY));
    }

    /**
     * Produce error view model and view.
     *
     * @param e the e
     * @return the model and view
     */
    public static ModelAndView produceErrorView(final Exception e) {
        final Map model = new HashMap<>();
        model.put("rootCauseException", e);
        return new ModelAndView(OAuth20Constants.ERROR_VIEW, model);
    }

    /**
     * Cas oauth callback url.
     *
     * @param serverPrefixUrl the server prefix url
     * @return the string
     */
    public static String casOAuthCallbackUrl(final String serverPrefixUrl) {
        return serverPrefixUrl.concat(BASE_OAUTH20_URL + '/' + OAuth20Constants.CALLBACK_AUTHORIZE_URL);
    }

    /**
     * Jsonify string.
     *
     * @param map the map
     * @return the string
     */
    @SneakyThrows
    public static String jsonify(final Map map) {
        return WRITER.writeValueAsString(map);
    }

    /**
     * Gets response type.
     *
     * @param context the context
     * @return the response type
     */
    public static OAuth20ResponseTypes getResponseType(final J2EContext context) {
        final String responseType = context.getRequestParameter(OAuth20Constants.RESPONSE_TYPE);
        final OAuth20ResponseTypes type = Arrays.stream(OAuth20ResponseTypes.values())
            .filter(t -> t.getType().equalsIgnoreCase(responseType))
            .findFirst()
            .orElse(OAuth20ResponseTypes.CODE);
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
     * Is authorized response type for service?
     *
     * @param context           the context
     * @param registeredService the registered service
     * @return the boolean
     */
    public static boolean isAuthorizedResponseTypeForService(final J2EContext context, final OAuthRegisteredService registeredService) {
        final String responseType = context.getRequestParameter(OAuth20Constants.RESPONSE_TYPE);
        if (registeredService.getSupportedResponseTypes() != null && !registeredService.getSupportedResponseTypes().isEmpty()) {
            LOGGER.debug("Checking response type [{}] against supported response types [{}]", responseType, registeredService.getSupportedResponseTypes());
            return registeredService.getSupportedResponseTypes().stream().anyMatch(s -> s.equalsIgnoreCase(responseType));
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
    public static boolean isAuthorizedGrantTypeForService(final J2EContext context, final OAuthRegisteredService registeredService) {
        return isAuthorizedGrantTypeForService(
                context.getRequestParameter(OAuth20Constants.GRANT_TYPE),
                registeredService);
    }

    /**
     * Parse request scopes set.
     *
     * @param context the context
     * @return the set
     */
    public static Set<String> parseRequestScopes(final J2EContext context) {
        return parseRequestScopes(context.getRequest());
    }

    /**
     * Parse request scopes set.
     *
     * @param context the context
     * @return the set
     */
    public static Set<String> parseRequestScopes(final HttpServletRequest context) {
        final String parameterValues = context.getParameter(OAuth20Constants.SCOPE);
        if (StringUtils.isBlank(parameterValues)) {
            return new HashSet<>(0);
        }
        return CollectionUtils.wrapSet(parameterValues.split(" "));
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
        String id = context.getHeader(CasProtocolConstants.PARAMETER_SERVICE);
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
    public static boolean checkCallbackValid(@NonNull final RegisteredService registeredService, final String redirectUri) {
        final String registeredServiceId = registeredService.getServiceId();
        LOGGER.debug("Found: [{}] vs redirectUri: [{}]", registeredService, redirectUri);
        if (!redirectUri.matches(registeredServiceId)) {
            LOGGER.error("Unsupported [{}]: [{}] does not match what is defined for registered service: [{}]. "
                    + "Service is considered unauthorized. Verify the service definition in the registry is correct "
                    + "and does in fact match the client [{}]",
                OAuth20Constants.REDIRECT_URI, redirectUri, registeredServiceId, redirectUri);
            return false;
        }
        return true;
    }

    /**
     * Check the client secret.
     *
     * @param registeredService the registered service
     * @param clientSecret      the client secret
     * @return whether the secret is valid
     */
    public static boolean checkClientSecret(final OAuthRegisteredService registeredService, final String clientSecret) {
        LOGGER.debug("Found: [{}] in secret check", registeredService);
        if (StringUtils.isBlank(registeredService.getClientSecret())) {
            LOGGER.debug("The client secret is not defined for the registered service [{}]", registeredService.getName());
            return true;
        }
        if (!StringUtils.equals(registeredService.getClientSecret(), clientSecret)) {
            LOGGER.error("Wrong client secret for service: [{}]", registeredService);
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
        final boolean checked = Stream.of(expectedTypes).anyMatch(t -> OAuth20Utils.isResponseType(type, t));
        if (!checked) {
            LOGGER.error("Unsupported response type: [{}]", type);
        }
        return checked;
    }
}
