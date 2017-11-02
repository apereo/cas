package org.apereo.cas.util;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
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
public final class HttpRequestUtils {
    /**
     * Constant representing the request header for user agent.
     */
    public static final String USER_AGENT_HEADER = "user-agent";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestUtils.class);
    
    private HttpRequestUtils() {}
    
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
        final int latIndex = 0;
        final int longIndex = 1;
        final int accuracyIndex = 2;
        final int timeIndex = 3;
        final GeoLocationRequest loc = new GeoLocationRequest();
        if (request != null) {
            final String geoLocationParam = request.getParameter("geolocation");
            if (StringUtils.isNotBlank(geoLocationParam)) {
                final String[] geoLocation = geoLocationParam.split(",");
                loc.setLatitude(geoLocation[latIndex]);
                loc.setLongitude(geoLocation[longIndex]);
                loc.setAccuracy(geoLocation[accuracyIndex]);
                loc.setTimestamp(geoLocation[timeIndex]);
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
        final Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                final String headerName = headerNames.nextElement();
                final String headerValue = StringUtils.stripToEmpty(request.getHeader(headerName));
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
    
    
}
