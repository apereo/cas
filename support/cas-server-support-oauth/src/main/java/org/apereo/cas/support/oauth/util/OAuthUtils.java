package org.apereo.cas.support.oauth.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpStatus;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.pac4j.core.context.J2EContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class has some usefull methods to output data in plain text,
 * handle redirects, add parameter in url or find the right provider.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
public final class OAuthUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthUtils.class);
    private static final ObjectWriter WRITER = new ObjectMapper().findAndRegisterModules().writer().withDefaultPrettyPrinter();

    private OAuthUtils() {
    }

    /**
     * Write to the output this error text and return a null view.
     *
     * @param response http response
     * @param error    error message
     * @return a null view
     */
    public static ModelAndView writeTextError(final HttpServletResponse response, final String error) {
        return OAuthUtils.writeText(response, OAuthConstants.ERROR + '=' + error, HttpStatus.SC_BAD_REQUEST);
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
     * Return a view which is a redirection to an url.
     *
     * @param url redirect url
     * @return A view which is a redirection to an url
     */
    public static ModelAndView redirectTo(final String url) {
        return new ModelAndView(new RedirectView(url));
    }

    /**
     * Locate the requested instance of {@link OAuthRegisteredService} by the given clientId.
     *
     * @param servicesManager the service registry DAO instance.
     * @param clientId        the client id by which the {@link OAuthRegisteredService} is to be located.
     * @return null, or the located {@link OAuthRegisteredService} instance in the service registry.
     */
    public static OAuthRegisteredService getRegisteredOAuthService(final ServicesManager servicesManager, final String clientId) {
        final Collection<RegisteredService> services = servicesManager.getAllServices();
        return (OAuthRegisteredService) services.stream()
                .filter(OAuthRegisteredService.class::isInstance)
                .filter(s -> OAuthRegisteredService.class.cast(s).getClientId().equals(clientId))
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
        final Map<String, Object> map = getRequestParameters(Arrays.asList(OAuthConstants.SCOPE), context);
        if (map == null || map.isEmpty()) {
            return Collections.emptyList();
        }
        return (Collection<String>) map.get(OAuthConstants.SCOPE);
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
        return new ModelAndView(OAuthConstants.ERROR_VIEW, model);
    }

    /**
     * Jsonify string.
     *
     * @param map the map
     * @return the string
     */
    public static String jsonify(final Map map) {
        try {
            return WRITER.writeValueAsString(map);
        } catch (final Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
