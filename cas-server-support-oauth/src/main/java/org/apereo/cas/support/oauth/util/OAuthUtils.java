package org.apereo.cas.support.oauth.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * This class has some usefull methods to output data in plain text,
 * handle redirects, add parameter in url or find the right provider.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
public final class OAuthUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthUtils.class);

    private OAuthUtils() {}

    /**
     * Write to the output this error text and return a null view.
     *
     * @param response http response
     * @param error error message
     * @return a null view
     */
    public static ModelAndView writeTextError(final HttpServletResponse response, final String error) {
        return OAuthUtils.writeText(response, OAuthConstants.ERROR + '=' + error, HttpStatus.SC_BAD_REQUEST);
    }

    /**
     * Write to the output the text and return a null view.
     *
     * @param response http response
     * @param text output text
     * @param status status code
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
     * @param servicesManager the service registry DAO instance.
     * @param clientId the client id by which the {@link OAuthRegisteredService} is to be located.
     * @return null, or the located {@link OAuthRegisteredService} instance in the service registry.
     */
    public static OAuthRegisteredService getRegisteredOAuthService(final ServicesManager servicesManager, final String clientId) {
        for (final RegisteredService aService : servicesManager.getAllServices()) {
            if (aService instanceof OAuthRegisteredService) {
                final OAuthRegisteredService service = (OAuthRegisteredService) aService;
                if (service.getClientId().equals(clientId)) {
                    return service;
                }
            }
        }
        return null;
    }

    /**
     * Jsonify string.
     *
     * @param map the map
     * @return the string
     */
    public static String jsonify(final Map map) {
        try {
            final String value = new ObjectMapper()
                    .writer()
                    .withDefaultPrettyPrinter()
                    .writeValueAsString(map);
            return value;
        } catch (final Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }
}
