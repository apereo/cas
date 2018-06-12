package org.apereo.cas.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

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
        return execute(url, method, basicAuthUsername, basicAuthPassword, new HashMap<>(), new HashMap<>());
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
        return execute(url, method, basicAuthUsername, basicAuthPassword, new HashMap<>(), headers);
    }

    /**
     * Execute http response.
     *
     * @param url    the url
     * @param method the method
     * @return the http response
     */
    public static HttpResponse execute(final String url, final String method) {
        return execute(url, method, null, null, new HashMap<>(), new HashMap<>());
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
    public static HttpResponse execute(final String url, final String method,
                                       final String basicAuthUsername,
                                       final String basicAuthPassword,
                                       final Map<String, Object> parameters,
                                       final Map<String, Object> headers,
                                       final String entity) {
        try {
            final var client = buildHttpClient(basicAuthUsername, basicAuthPassword);
            final var uri = buildHttpUri(url, parameters);
            final HttpUriRequest request;
            switch (method.toLowerCase()) {
                case "post":
                    request = new HttpPost(uri);
                    if (StringUtils.isNotBlank(entity)) {
                        final var stringEntity = new StringEntity(entity);
                        ((HttpPost) request).setEntity(stringEntity);
                    }
                    break;
                case "delete":
                    request = new HttpDelete(uri);
                    break;
                case "get":
                default:
                    request = new HttpGet(uri);
                    break;
            }
            headers.forEach((k, v) -> request.addHeader(k, v.toString()));
            prepareHttpRequest(request, basicAuthUsername, basicAuthPassword, parameters);
            return client.execute(request);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
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
     * @return the http response
     */
    public static HttpResponse executeGet(final String url,
                                          final String basicAuthUsername,
                                          final String basicAuthPassword,
                                          final Map<String, Object> parameters) {
        try {
            return executeGet(url, basicAuthUsername, basicAuthPassword, parameters, new HashMap<>());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
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
            LOGGER.error(e.getMessage(), e);
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
            LOGGER.error(e.getMessage(), e);
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
            return executeGet(url, null, null, new LinkedHashMap<>());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
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
            return executeGet(url, basicAuthUsername, basicAuthPassword, new HashMap<>());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Execute post http response.
     *
     * @param url               the url
     * @param basicAuthUsername the basic auth username
     * @param basicAuthPassword the basic auth password
     * @param jsonEntity        the json entity
     * @return the http response
     */
    public static HttpResponse executePost(final String url,
                                           final String basicAuthUsername,
                                           final String basicAuthPassword,
                                           final String jsonEntity) {
        return executePost(url, basicAuthUsername, basicAuthPassword, jsonEntity, new HashMap<>());
    }

    /**
     * Execute post http response.
     *
     * @param url        the url
     * @param jsonEntity the json entity
     * @param parameters the parameters
     * @return the http response
     */
    public static HttpResponse executePost(final String url,
                                           final String jsonEntity,
                                           final Map<String, Object> parameters) {
        return executePost(url, null, null, jsonEntity, parameters);
    }

    /**
     * Execute post http response.
     *
     * @param url               the url
     * @param basicAuthUsername the basic auth username
     * @param basicAuthPassword the basic auth password
     * @param jsonEntity        the json entity
     * @param parameters        the parameters
     * @return the http response
     */
    public static HttpResponse executePost(final String url,
                                           final String basicAuthUsername,
                                           final String basicAuthPassword,
                                           final String jsonEntity,
                                           final Map<String, Object> parameters) {
        try {
            return execute(url, HttpMethod.POST.name(), basicAuthUsername, basicAuthPassword, parameters, new HashMap<>(), jsonEntity);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Prepare credentials if needed.
     *
     * @param builder           the builder
     * @param basicAuthUsername username for basic auth
     * @param basicAuthPassword password for basic auth
     * @return the http client builder
     */
    private static HttpClientBuilder prepareCredentialsIfNeeded(final HttpClientBuilder builder, final String basicAuthUsername,
                                                                final String basicAuthPassword) {
        if (StringUtils.isNotBlank(basicAuthUsername) && StringUtils.isNotBlank(basicAuthPassword)) {
            final CredentialsProvider provider = new BasicCredentialsProvider();
            final var credentials = new UsernamePasswordCredentials(basicAuthUsername, basicAuthPassword);
            provider.setCredentials(AuthScope.ANY, credentials);
            return builder.setDefaultCredentialsProvider(provider);
        }
        return builder;
    }

    /**
     * Prepare http request. Tries to set the authorization header
     * in cases where the URL endpoint does not actually produce the header
     * on its own.
     *
     * @param request           the request
     * @param basicAuthUsername the basic auth username
     * @param basicAuthPassword the basic auth password
     * @param parameters        the parameters
     */
    private static void prepareHttpRequest(final HttpUriRequest request, final String basicAuthUsername,
                                           final String basicAuthPassword, final Map<String, Object> parameters) {
        if (StringUtils.isNotBlank(basicAuthUsername) && StringUtils.isNotBlank(basicAuthPassword)) {
            final var auth = EncodingUtils.encodeBase64(basicAuthUsername + ':' + basicAuthPassword);
            request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + auth);
        }
    }

    private static URI buildHttpUri(final String url, final Map<String, Object> parameters) throws URISyntaxException {
        final var uriBuilder = new URIBuilder(url);
        parameters.forEach((k, v) -> uriBuilder.addParameter(k, v.toString()));
        return uriBuilder.build();
    }

    private static HttpClient buildHttpClient(final String basicAuthUsername, final String basicAuthPassword) {
        final var builder = HttpClientBuilder.create();
        return prepareCredentialsIfNeeded(builder, basicAuthUsername, basicAuthPassword).build();
    }


    /**
     * Create headers org . springframework . http . http headers.
     *
     * @param basicAuthUser     the basic auth user
     * @param basicAuthPassword the basic auth password
     * @return the org . springframework . http . http headers
     */
    public static org.springframework.http.HttpHeaders createBasicAuthHeaders(final String basicAuthUser, final String basicAuthPassword) {
        final var acceptHeaders = new org.springframework.http.HttpHeaders();
        acceptHeaders.setAccept(CollectionUtils.wrap(MediaType.APPLICATION_JSON));
        if (StringUtils.isNotBlank(basicAuthUser) && StringUtils.isNotBlank(basicAuthPassword)) {
            final var authorization = basicAuthUser + ':' + basicAuthPassword;
            final var basic = EncodingUtils.encodeBase64(authorization.getBytes(Charset.forName("US-ASCII")));
            acceptHeaders.set("Authorization", "Basic " + basic);
        }
        return acceptHeaders;
    }
}
