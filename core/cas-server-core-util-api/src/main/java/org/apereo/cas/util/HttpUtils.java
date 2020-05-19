package org.apereo.cas.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link HttpUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@UtilityClass
public class HttpUtils {
    private static final int MAP_SIZE = 8;

    private static final int MAX_CONNECTIONS = 200;

    private static final int MAX_CONNECTIONS_PER_ROUTE = 20;

    private static HttpClient HTTP_CLIENT = HttpClientBuilder.create()
        .setMaxConnTotal(MAX_CONNECTIONS)
        .setMaxConnPerRoute(MAX_CONNECTIONS_PER_ROUTE)
        .build();

    /**
     * Execute http response.
     *
     * @param url               the url
     * @param method            the method
     * @param basicAuthUsername the basic auth username
     * @param basicAuthPassword the basic auth password
     * @param entity            the entity
     * @return the http response
     */
    public static HttpResponse execute(final String url,
                                       final String method,
                                       final String basicAuthUsername,
                                       final String basicAuthPassword,
                                       final String entity) {
        return execute(url, method, basicAuthUsername, basicAuthPassword,
            new HashMap<>(0), new HashMap<>(0), entity);
    }

    /**
     * Execute http response.
     *
     * @param url               the url
     * @param method            the method
     * @param basicAuthUsername the basic auth username
     * @param basicAuthPassword the basic auth password
     * @return the http response
     */
    public static HttpResponse execute(final String url, final String method,
                                       final String basicAuthUsername,
                                       final String basicAuthPassword) {
        return execute(url, method, basicAuthUsername, basicAuthPassword, new HashMap<>(0), new HashMap<>(0));
    }

    /**
     * Execute http response.
     *
     * @param url               the url
     * @param method            the method
     * @param basicAuthUsername the basic auth username
     * @param basicAuthPassword the basic auth password
     * @param headers           the headers
     * @return the http response
     */
    public static HttpResponse execute(final String url, final String method,
                                       final String basicAuthUsername, final String basicAuthPassword,
                                       final Map<String, Object> headers) {
        return execute(url, method, basicAuthUsername, basicAuthPassword, new HashMap<>(0), headers);
    }

    /**
     * Execute http response.
     *
     * @param url     the url
     * @param method  the method
     * @param headers the headers
     * @return the http response
     */
    public static HttpResponse execute(final String url, final String method,
                                       final Map<String, Object> headers) {
        return execute(url, method, null, null, new HashMap<>(0), headers);
    }

    /**
     * Execute http response.
     *
     * @param url    the url
     * @param method the method
     * @return the http response
     */
    public static HttpResponse execute(final String url, final String method) {
        return execute(url, method, null, null, new HashMap<>(0), new HashMap<>(0));
    }

    /**
     * Execute http response.
     *
     * @param url               the url
     * @param method            the method
     * @param basicAuthUsername the basic auth username
     * @param basicAuthPassword the basic auth password
     * @param parameters        the parameters
     * @param headers           the headers
     * @return the http response
     */
    public static HttpResponse execute(final String url, final String method,
                                       final String basicAuthUsername,
                                       final String basicAuthPassword,
                                       final Map<String, Object> parameters,
                                       final Map<String, Object> headers) {
        return execute(url, method, basicAuthUsername, basicAuthPassword, parameters, headers, null);
    }

    /**
     * Execute http request and produce a response.
     *
     * @param url               the url
     * @param method            the method
     * @param basicAuthUsername the basic auth username
     * @param basicAuthPassword the basic auth password
     * @param parameters        the parameters
     * @param headers           the headers
     * @param entity            the entity
     * @return the http response
     */
    public static HttpResponse execute(final String url,
                                       final String method,
                                       final String basicAuthUsername,
                                       final String basicAuthPassword,
                                       final Map<String, Object> parameters,
                                       final Map<String, Object> headers,
                                       final String entity) {
        try {
            val uri = buildHttpUri(url.trim(), parameters);
            val request = getHttpRequestByMethod(method.toLowerCase().trim(), entity, uri);
            headers.forEach((k, v) -> request.addHeader(k, v.toString()));
            prepareHttpRequest(request, basicAuthUsername, basicAuthPassword);
            return HTTP_CLIENT.execute(request);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return null;
    }

    /**
     * Close the response.
     *
     * @param response the response to close
     */
    public static void close(final HttpResponse response) {
        if (response instanceof CloseableHttpResponse) {
            val closeableHttpResponse = (CloseableHttpResponse) response;
            try {
                closeableHttpResponse.close();
            } catch (final IOException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.error(e.getMessage(), e);
                } else {
                    LOGGER.error(e.getMessage());
                }
            }
        }
    }

    /**
     * Execute get http response.
     *
     * @param url               the url
     * @param basicAuthUsername the basic auth username
     * @param basicAuthPassword the basic auth password
     * @param parameters        the parameters
     * @return the http response
     */
    public static HttpResponse executeGet(final String url,
                                          final String basicAuthUsername,
                                          final String basicAuthPassword,
                                          final Map<String, Object> parameters) {
        try {
            return executeGet(url, basicAuthUsername, basicAuthPassword, parameters, new HashMap<>(0));
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return null;
    }

    /**
     * Execute get http response.
     *
     * @param url               the url
     * @param basicAuthUsername the basic auth username
     * @param basicAuthPassword the basic auth password
     * @param parameters        the parameters
     * @param headers           the headers
     * @return the http response
     */
    public static HttpResponse executeGet(final String url,
                                          final String basicAuthUsername,
                                          final String basicAuthPassword,
                                          final Map<String, Object> parameters,
                                          final Map<String, Object> headers) {
        try {
            return execute(url, HttpMethod.GET.name(), basicAuthUsername, basicAuthPassword, parameters, headers);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return null;
    }

    /**
     * Execute get http response.
     *
     * @param url        the url
     * @param parameters the parameters
     * @param headers    the headers
     * @return the http response
     */
    public static HttpResponse executeGet(final String url,
                                          final Map<String, Object> parameters,
                                          final Map<String, Object> headers) {
        try {
            return execute(url, HttpMethod.GET.name(), null, null, parameters, headers);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return null;
    }

    /**
     * Execute get http response.
     *
     * @param url        the url
     * @param parameters the parameters
     * @return the http response
     */
    public static HttpResponse executeGet(final String url,
                                          final Map<String, Object> parameters) {
        try {
            return executeGet(url, null, null, parameters);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return null;
    }

    /**
     * Execute get http response.
     *
     * @param url the url
     * @return the http response
     */
    public static HttpResponse executeGet(final String url) {
        try {
            return executeGet(url, null, null, new LinkedHashMap<>(MAP_SIZE));
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return null;
    }

    /**
     * Execute get http response.
     *
     * @param url               the url
     * @param basicAuthUsername the basic auth username
     * @param basicAuthPassword the basic auth password
     * @return the http response
     */
    public static HttpResponse executeGet(final String url,
                                          final String basicAuthUsername,
                                          final String basicAuthPassword) {
        try {
            return executeGet(url, basicAuthUsername, basicAuthPassword, new HashMap<>(0));
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return null;
    }

    /**
     * Execute post http response.
     *
     * @param url               the url
     * @param basicAuthUsername the basic auth username
     * @param basicAuthPassword the basic auth password
     * @param entity            the json entity
     * @return the http response
     */
    public static HttpResponse executePost(final String url,
                                           final String basicAuthUsername,
                                           final String basicAuthPassword,
                                           final String entity) {
        return executePost(url, basicAuthUsername, basicAuthPassword, entity, new HashMap<>(0));
    }

    /**
     * Execute post http response.
     *
     * @param url        the url
     * @param entity     the json entity
     * @param parameters the parameters
     * @return the http response
     */
    public static HttpResponse executePost(final String url,
                                           final String entity,
                                           final Map<String, Object> parameters) {
        return executePost(url, null, null, entity, parameters);
    }

    /**
     * Execute post http response.
     *
     * @param url               the url
     * @param basicAuthUsername the basic auth username
     * @param basicAuthPassword the basic auth password
     * @param entity            the json entity
     * @param parameters        the parameters
     * @return the http response
     */
    public static HttpResponse executePost(final String url,
                                           final String basicAuthUsername,
                                           final String basicAuthPassword,
                                           final String entity,
                                           final Map<String, Object> parameters) {
        try {
            return executePost(url, basicAuthUsername, basicAuthPassword, entity, parameters, new HashMap<>(0));
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return null;
    }

    /**
     * Execute post http response.
     *
     * @param url               the url
     * @param basicAuthUsername the basic auth username
     * @param basicAuthPassword the basic auth password
     * @param entity            the entity
     * @param parameters        the parameters
     * @param headers           the headers
     * @return the http response
     */
    public static HttpResponse executePost(final String url,
                                           final String basicAuthUsername,
                                           final String basicAuthPassword,
                                           final String entity,
                                           final Map<String, Object> parameters,
                                           final Map<String, Object> headers) {
        try {
            return execute(url, HttpMethod.POST.name(), basicAuthUsername, basicAuthPassword, parameters, headers, entity);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return null;
    }

    /**
     * Execute post http response.
     *
     * @param url        the url
     * @param parameters the parameters
     * @param headers    the headers
     * @return the http response
     */
    public static HttpResponse executePost(final String url,
                                           final Map<String, Object> parameters,
                                           final Map<String, Object> headers) {
        try {
            return execute(url, HttpMethod.POST.name(), null, null, parameters, headers, null);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return null;
    }

    /**
     * Execute delete http response.
     *
     * @param url               the url
     * @param basicAuthUsername the basic auth username
     * @param basicAuthPassword the basic auth password
     * @param entity            the entity
     * @param parameters        the parameters
     * @param headers           the headers
     * @return the http response
     */
    public static HttpResponse executeDelete(final String url,
                                             final String basicAuthUsername,
                                             final String basicAuthPassword,
                                             final String entity,
                                             final Map<String, Object> parameters,
                                             final Map<String, Object> headers) {
        try {
            return execute(url, HttpMethod.DELETE.name(), basicAuthUsername, basicAuthPassword, parameters, headers, entity);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return null;
    }

    /**
     * Execute delete http response.
     *
     * @param url               the url
     * @param basicAuthUsername the basic auth username
     * @param basicAuthPassword the basic auth password
     * @param parameters        the parameters
     * @param headers           the headers
     * @return the http response
     */
    public static HttpResponse executeDelete(final String url,
                                             final String basicAuthUsername,
                                             final String basicAuthPassword,
                                             final Map<String, Object> parameters,
                                             final Map<String, Object> headers) {
        try {
            return executeDelete(url, basicAuthUsername, basicAuthPassword, null, parameters, headers);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return null;
    }

    /**
     * Execute delete http response.
     *
     * @param url               the url
     * @param basicAuthUsername the basic auth username
     * @param basicAuthPassword the basic auth password
     * @return the http response
     */
    public static HttpResponse executeDelete(final String url,
                                             final String basicAuthUsername,
                                             final String basicAuthPassword) {
        try {
            return executeDelete(url, basicAuthUsername, basicAuthPassword, null, null, null);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return null;
    }

    /**
     * Create headers org . springframework . http . http headers.
     *
     * @param basicAuthUser     the basic auth user
     * @param basicAuthPassword the basic auth password
     * @return the org . springframework . http . http headers
     */
    public static org.springframework.http.HttpHeaders createBasicAuthHeaders(final String basicAuthUser, final String basicAuthPassword) {
        return HttpUtils.createBasicAuthHeaders(basicAuthUser, basicAuthPassword, "US-ASCII");
    }

    /**
     * Create headers org . springframework . http . http headers.
     *
     * @param basicAuthUser     the basic auth user
     * @param basicAuthPassword the basic auth password
     * @param basicCharset      The charset used to encode auth header
     * @return the org . springframework . http . http headers
     */
    public static org.springframework.http.HttpHeaders createBasicAuthHeaders(final String basicAuthUser, final String basicAuthPassword, final String basicCharset) {
        val acceptHeaders = new org.springframework.http.HttpHeaders();
        acceptHeaders.setAccept(CollectionUtils.wrap(MediaType.APPLICATION_JSON));
        if (StringUtils.isNotBlank(basicAuthUser) && StringUtils.isNotBlank(basicAuthPassword)) {
            val authorization = basicAuthUser + ':' + basicAuthPassword;
            val basic = EncodingUtils.encodeBase64(authorization.getBytes(Charset.forName(basicCharset)));
            acceptHeaders.set(org.springframework.http.HttpHeaders.AUTHORIZATION, "Basic " + basic);
        }
        return acceptHeaders;
    }

    public static HttpClient getHttpClient() {
        return HTTP_CLIENT;
    }

    public static void setHttpClient(final HttpClient httpClient) {
        HTTP_CLIENT = httpClient;
    }

    @SneakyThrows
    private static HttpUriRequest getHttpRequestByMethod(final String method, final String entity, final URI uri) {
        if ("post".equalsIgnoreCase(method)) {
            val request = new HttpPost(uri);
            if (StringUtils.isNotBlank(entity)) {
                val stringEntity = new StringEntity(entity);
                request.setEntity(stringEntity);
            }
            return request;
        }
        if ("delete".equalsIgnoreCase(method)) {
            return new HttpDelete(uri);
        }

        return new HttpGet(uri);
    }

    /**
     * Prepare http request. Tries to set the authorization header
     * in cases where the URL endpoint does not actually produce the header
     * on its own.
     *
     * @param request           the request
     * @param basicAuthUsername the basic auth username
     * @param basicAuthPassword the basic auth password
     */
    private static void prepareHttpRequest(final HttpUriRequest request, final String basicAuthUsername,
                                           final String basicAuthPassword) {
        if (StringUtils.isNotBlank(basicAuthUsername) && StringUtils.isNotBlank(basicAuthPassword)) {
            val auth = EncodingUtils.encodeBase64(basicAuthUsername + ':' + basicAuthPassword);
            request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + auth);
        }
    }

    private static URI buildHttpUri(final String url, final Map<String, Object> parameters) throws URISyntaxException {
        val uriBuilder = new URIBuilder(url);
        parameters.forEach((k, v) -> uriBuilder.addParameter(k, v.toString()));
        return uriBuilder.build();
    }
}
