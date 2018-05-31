package org.apereo.cas.util;

import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This is {@link HttpRequestUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */

@Slf4j
public class HttpRequestUtils {
    /**
     * Constant representing the request header for user agent.
     */
    public static final String USER_AGENT_HEADER = "user-agent";

    /**
     * Gets http servlet request from request attributes.
     *
     * @return the http servlet request from request attributes
     */
    public static HttpServletRequest getHttpServletRequestFromRequestAttributes() {
        try {
            return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Gets http servlet response from request attributes.
     *
     * @return the http servlet response from request attributes
     */
    public static HttpServletResponse getHttpServletResponseFromRequestAttributes() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
    }

    /**
     * Gets http servlet request geo location.
     *
     * @param request the request
     * @return the http servlet request geo location
     */
    public static GeoLocationRequest getHttpServletRequestGeoLocation(final HttpServletRequest request) {
        final var latIndex = 0;
        final var longIndex = 1;
        final var accuracyIndex = 2;
        final var timeIndex = 3;
        final var loc = new GeoLocationRequest();
        if (request != null) {
            final var geoLocationParam = request.getParameter("geolocation");
            if (StringUtils.isNotBlank(geoLocationParam)) {
                final var geoLocation = Splitter.on(",").splitToList(geoLocationParam);
                loc.setLatitude(geoLocation.get(latIndex));
                loc.setLongitude(geoLocation.get(longIndex));
                loc.setAccuracy(geoLocation.get(accuracyIndex));
                loc.setTimestamp(geoLocation.get(timeIndex));
            }
        }
        return loc;
    }

    /**
     * Gets request headers.
     *
     * @param request the request
     * @return the request headers
     */
    public static Map<String, String> getRequestHeaders(final HttpServletRequest request) {
        final Map<String, String> headers = new LinkedHashMap<>();
        final var headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                final var headerName = headerNames.nextElement();
                final var headerValue = StringUtils.stripToEmpty(request.getHeader(headerName));
                headers.put(headerName, headerValue);
            }
        }
        return headers;
    }

    /**
     * Gets http servlet request user agent.
     *
     * @param request the request
     * @return the http servlet request user agent
     */
    public static String getHttpServletRequestUserAgent(final HttpServletRequest request) {
        if (request != null) {
            return request.getHeader(USER_AGENT_HEADER);
        }
        return null;
    }

    /**
     * Gets the service from the request based on given extractors.
     *
     * @param argumentExtractors the argument extractors
     * @param request            the request
     * @return the service, or null.
     */
    public static WebApplicationService getService(final List<ArgumentExtractor> argumentExtractors, final HttpServletRequest request) {
        return argumentExtractors.stream().map(argumentExtractor -> argumentExtractor.extractService(request))
            .filter(Objects::nonNull).findFirst().orElse(null);
    }

    /**
     * Check if a parameter exists.
     *
     * @param request the HTTP request
     * @param name    the parameter name
     * @return whether the parameter exists
     */
    public static boolean doesParameterExist(final HttpServletRequest request, final String name) {
        final var parameter = request.getParameter(name);
        if (StringUtils.isBlank(parameter)) {
            LOGGER.error("Missing request parameter: [{}]", name);
            return false;
        }
        LOGGER.debug("Found provided request parameter [{}]", name);
        return true;
    }

}
