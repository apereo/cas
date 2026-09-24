package org.apereo.cas.util.http;

import module java.base;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.support.ArgumentExtractor;
import com.google.common.base.Splitter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpVersion;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.io.ModalCloseable;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
     * Number of bytes read from a response body before the read is abandoned, unless the request
     * asks for a different bound. Every response this class hands back is buffered in memory in
     * full, so without a ceiling the size of a response is decided by whoever is answering, and a
     * server that keeps sending takes the heap with it. Overridable through the
     * {@code org.apereo.cas.util.http.HttpRequestUtils.maximumResponseSize} system property, in the
     * same way this package resolves its timeouts. The default is deliberately generous: it is
     * meant to bound a runaway response, not to police a legitimately large one such as a SAML
     * federation aggregate.
     */
    private static final long DEFAULT_MAXIMUM_RESPONSE_SIZE = resolveDefaultMaximumResponseSize();

    private static final int COPY_BUFFER_SIZE = 8192;

    private static final int GEO_LOC_LONG_INDEX = 1;

    private static final int GEO_LOC_ACCURACY_INDEX = 2;

    private static final int GEO_LOC_TIME_INDEX = 3;

    private static final int PING_URL_TIMEOUT = 3_000;

    /**
     * HTTP client response handler that simply returns the classic HTTP response.
     * This is useful for cases where you want to handle the response without any additional processing.
     */
    public static final HttpClientResponseHandler<ClassicHttpResponse> HTTP_CLIENT_RESPONSE_HANDLER =
        responseHandler(DEFAULT_MAXIMUM_RESPONSE_SIZE);

    /**
     * Builds a response handler that buffers the response body, refusing to read beyond the given
     * number of bytes. The executing client owns entity cleanup. Failed downloads are closed
     * immediately so graceful stream cleanup cannot drain an oversized or endless response.
     *
     * @param maximumResponseSize the ceiling in bytes; a value of zero or less means the default
     * @return the response handler
     */
    public static HttpClientResponseHandler<ClassicHttpResponse> responseHandler(final long maximumResponseSize) {
        val maximumSize = maximumResponseSize > 0 ? maximumResponseSize : DEFAULT_MAXIMUM_RESPONSE_SIZE;
        return response -> {
            val result = new BasicClassicHttpResponse(response.getCode(), response.getReasonPhrase());
            result.setHeaders(response.getHeaders());
            result.setLocale(ObjectUtils.getIfNull(response.getLocale(), Locale.getDefault()));
            result.setVersion(ObjectUtils.getIfNull(response.getVersion(), HttpVersion.HTTP_1_1));

            val entity = response.getEntity();
            if (entity != null) {
                try (val output = new ByteArrayOutputStream()) {
                    try {
                        if (entity.getContentLength() > maximumSize) {
                            throw new IOException("Response body of %s bytes exceeds the maximum permitted size of %s bytes"
                                .formatted(entity.getContentLength(), maximumSize));
                        }
                        copyBounded(entity.getContent(), output, maximumSize);
                    } catch (final IOException e) {
                        if (response instanceof final ModalCloseable closeable) {
                            closeable.close(CloseMode.IMMEDIATE);
                        }
                        throw e;
                    }
                    val contentTypeHeader = response.getFirstHeader(HttpHeaders.CONTENT_TYPE);
                    val contentType = contentTypeHeader != null ? contentTypeHeader.getValue() : ContentType.APPLICATION_JSON.getMimeType();
                    result.setEntity(new ByteArrayEntity(output.toByteArray(), ContentType.parseLenient(contentType)));
                }
            }

            return result;
        };
    }

    private static long resolveDefaultMaximumResponseSize() {
        val configured = System.getProperty(HttpRequestUtils.class.getName() + ".maximumResponseSize");
        val maximumSize = Long.parseLong(StringUtils.defaultIfBlank(configured, "268435456"));
        if (maximumSize <= 0) {
            throw new IllegalArgumentException("The default maximum HTTP response size must be positive");
        }
        return maximumSize;
    }

    /**
     * Copies the body across, counting as it goes so that a response with no declared length, or a
     * dishonest one, still cannot buffer past the ceiling. At most one additional byte is read
     * to distinguish a response at the limit from an oversized response.
     *
     * @param input       the response body
     * @param output      the buffer to fill
     * @param maximumSize the ceiling in bytes
     * @throws IOException when the body runs past the ceiling
     */
    private static void copyBounded(final InputStream input, final OutputStream output,
                                    final long maximumSize) throws IOException {
        val buffer = new byte[COPY_BUFFER_SIZE];
        var total = 0L;
        while (total < maximumSize) {
            val count = input.read(buffer, 0, (int) Math.min(buffer.length, maximumSize - total));
            if (count < 0) {
                return;
            }
            output.write(buffer, 0, count);
            total += count;
        }
        if (input.read() != -1) {
            throw new IOException("Response body exceeds the maximum permitted size of %s bytes".formatted(maximumSize));
        }
    }

    /**
     * Gets http servlet request from request attributes.
     *
     * @return the http servlet request from request attributes
     */
    public static @Nullable HttpServletRequest getHttpServletRequestFromRequestAttributes() {
        val requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return Optional.ofNullable(requestAttributes).map(ServletRequestAttributes::getRequest).orElse(null);
    }

    /**
     * Gets http servlet response from request attributes.
     *
     * @return the http servlet response from request attributes
     */
    public static @Nullable HttpServletResponse getHttpServletResponseFromRequestAttributes() {
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
        if (StringUtils.isNotBlank(geoLocationParam) && !Strings.CI.equals(geoLocationParam, "unknown")) {
            val geoLocation = Splitter.on(",").splitToList(geoLocationParam);
            if (!geoLocation.isEmpty()) {
                loc.setLatitude(geoLocation.getFirst());
            }
            if (geoLocation.size() >= GEO_LOC_LONG_INDEX + 1) {
                loc.setLongitude(geoLocation.get(GEO_LOC_LONG_INDEX));
            }
            if (geoLocation.size() >= GEO_LOC_ACCURACY_INDEX + 1) {
                loc.setAccuracy(geoLocation.get(GEO_LOC_ACCURACY_INDEX));
            }
            if (geoLocation.size() >= GEO_LOC_TIME_INDEX + 1) {
                loc.setTimestamp(geoLocation.get(GEO_LOC_TIME_INDEX));
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
    @SuppressWarnings("JdkObsolete")
    public static Map<String, String> getRequestHeaders(final HttpServletRequest request) {
        val unsafeRequestHeaders = Set.of(
            HttpHeaders.COOKIE.toLowerCase(Locale.ENGLISH),
            HttpHeaders.SET_COOKIE.toLowerCase(Locale.ENGLISH),
            HttpHeaders.AUTHORIZATION.toLowerCase(Locale.ENGLISH),
            HttpHeaders.PROXY_AUTHORIZATION.toLowerCase(Locale.ENGLISH)
        );

        val headers = new LinkedHashMap<String, Object>();
        if (request != null) {
            val headerNames = request.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    val headerName = headerNames.nextElement();
                    val headerValue = StringUtils.stripToEmpty(request.getHeader(headerName));
                    if (!unsafeRequestHeaders.contains(headerName.toLowerCase(Locale.ENGLISH))) {
                        headers.put(headerName, headerValue);
                    }
                }
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
    public static @Nullable String getHttpServletRequestUserAgent(final @Nullable HttpServletRequest request) {
        if (request != null) {
            return request.getHeader(HttpHeaders.USER_AGENT);
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
    public static @Nullable WebApplicationService getService(final List<ArgumentExtractor> argumentExtractors,
                                                             final HttpServletRequest request) {
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
        HttpURLConnection connection = null;
        try {
            val url = new URI(location).toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(PING_URL_TIMEOUT);
            connection.setReadTimeout(PING_URL_TIMEOUT);
            connection.setRequestMethod(HttpMethod.GET.name());
            connection.setInstanceFollowRedirects(false);
            connection.setUseCaches(false);
            connection.setRequestProperty("Range", "bytes=0-0");
            return HttpStatus.valueOf(connection.getResponseCode());
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return HttpStatus.SERVICE_UNAVAILABLE;

    }

    /**
     * Gets full request url.
     *
     * @param request the request
     * @return the full request url
     */
    public static String getFullRequestUrl(final HttpServletRequest request) {
        return request.getRequestURL() + (request.getQueryString() != null ? '?' + request.getQueryString() : StringUtils.EMPTY);
    }
}
