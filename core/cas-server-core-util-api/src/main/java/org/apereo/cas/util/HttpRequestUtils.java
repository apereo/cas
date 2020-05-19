package org.apereo.cas.util;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.web.support.ArgumentExtractor;

import com.google.common.base.Splitter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link HttpRequestUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@UtilityClass
@Slf4j
public class HttpRequestUtils {
    /**
     * Constant representing the request header for user agent.
     */
    public static final String USER_AGENT_HEADER = "user-agent";

    private static final int GEO_LOC_LAT_INDEX = 0;
    private static final int GEO_LOC_LONG_INDEX = 1;
    private static final int GEO_LOC_ACCURACY_INDEX = 2;
    private static final int GEO_LOC_TIME_INDEX = 3;

    private static final int PING_URL_TIMEOUT = 5_000;

    /**
     * Gets http servlet request from request attributes.
     *
     * @return the http servlet request from request attributes
     */
    public static HttpServletRequest getHttpServletRequestFromRequestAttributes() {
        try {
            val requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return Optional.ofNullable(requestAttributes).map(ServletRequestAttributes::getRequest).orElse(null);
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
        val requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return Optional.ofNullable(requestAttributes).map(ServletRequestAttributes::getResponse).orElse(null);
    }

    /**
     * Gets http servlet request geo location.
     *
     * @param request the request
     * @return the http servlet request geo location
     */
    public static GeoLocationRequest getHttpServletRequestGeoLocation(final HttpServletRequest request) {
        val loc = new GeoLocationRequest();
        if (request != null) {
            val geoLocationParam = request.getParameter("geolocation");
            return getHttpServletRequestGeoLocation(geoLocationParam);
        }
        return loc;
    }

    /**
     * Gets http servlet request geo location.
     *
     * @param geoLocationParam the geo location param
     * @return the http servlet request geo location
     */
    public static GeoLocationRequest getHttpServletRequestGeoLocation(final String geoLocationParam) {
        val loc = new GeoLocationRequest();
        if (StringUtils.isNotBlank(geoLocationParam) && !StringUtils.equalsIgnoreCase(geoLocationParam, "unknown")) {
            val geoLocation = Splitter.on(",").splitToList(geoLocationParam);
            loc.setLatitude(geoLocation.get(GEO_LOC_LAT_INDEX));
            loc.setLongitude(geoLocation.get(GEO_LOC_LONG_INDEX));
            loc.setAccuracy(geoLocation.get(GEO_LOC_ACCURACY_INDEX));
            loc.setTimestamp(geoLocation.get(GEO_LOC_TIME_INDEX));

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
        val headers = new LinkedHashMap<String, Object>();
        val headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                val headerName = headerNames.nextElement();
                val headerValue = StringUtils.stripToEmpty(request.getHeader(headerName));
                headers.put(headerName, headerValue);
            }
        }
        return (Map) headers;
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
        return argumentExtractors
            .stream()
            .map(argumentExtractor -> argumentExtractor.extractService(request))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    /**
     * Check if a parameter exists.
     *
     * @param request the HTTP request
     * @param name    the parameter name
     * @return whether the parameter exists
     */
    public static boolean doesParameterExist(final HttpServletRequest request, final String name) {
        val parameter = request.getParameter(name);
        if (StringUtils.isBlank(parameter)) {
            LOGGER.error("Missing request parameter: [{}]", name);
            return false;
        }
        LOGGER.debug("Found provided request parameter [{}]", name);
        return true;
    }

    /**
     * Ping url and return http status.
     *
     * @param location the location
     * @return the http status
     */
    public static HttpStatus pingUrl(final String location) {
        try {
            val url = new URL(location);
            val connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(PING_URL_TIMEOUT);
            connection.setReadTimeout(PING_URL_TIMEOUT);
            connection.setRequestMethod(HttpMethod.HEAD.name());
            return HttpStatus.valueOf(connection.getResponseCode());
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return HttpStatus.SERVICE_UNAVAILABLE;

    }
}
