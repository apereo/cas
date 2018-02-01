package org.apereo.cas.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
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
     * Execute http request and produce a response.
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
                                       final Map<String, String> parameters,
                                       final Map<String, String> headers) {
        try {
            final HttpClient client = buildHttpClient(basicAuthUsername, basicAuthPassword);
            final URI uri = buildHttpUri(url, parameters);
            final HttpUriRequest request = method.equalsIgnoreCase(HttpMethod.GET.name()) ? new HttpGet(uri) : new HttpPost(uri);
            headers.forEach(request::addHeader);
            return client.execute(request);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private static URI buildHttpUri(final String url, final Map<String, String> parameters) throws URISyntaxException {
        final URIBuilder uriBuilder = new URIBuilder(url);
        parameters.forEach(uriBuilder::addParameter);
        return uriBuilder.build();
    }

    private static HttpClient buildHttpClient(final String basicAuthUsername, final String basicAuthPassword) {
        final HttpClientBuilder builder = HttpClientBuilder.create();
        return prepareCredentialsIfNeeded(builder, basicAuthUsername, basicAuthPassword).build();
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
                                          final Map<String, String> parameters) {
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
                                          final Map<String, String> parameters,
                                          final Map<String, String> headers) {
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
                                          final Map<String, String> parameters) {
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
     * @param parameters        the parameters
     * @return the http response
     */
    public static HttpResponse executePost(final String url,
                                           final String basicAuthUsername,
                                           final String basicAuthPassword,
                                           final Map<String, String> parameters) {
        try {
            return execute(url, basicAuthPassword, basicAuthUsername, HttpMethod.POST.name(), parameters, new HashMap<>());
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
     * @param entity            the entity
     * @return the http response
     */
    public static HttpResponse executePost(final String url,
                                           final String basicAuthUsername,
                                           final String basicAuthPassword,
                                           final HttpEntity entity) {
        try {
            final HttpClient client = buildHttpClient(basicAuthUsername, basicAuthPassword);
            final URI uri = buildHttpUri(url, new HashMap<>());
            final HttpPost request = new HttpPost(uri);
            request.setEntity(entity);
            return client.execute(request);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Execute post http response.
     *
     * @param url        the url
     * @param entity     the entity
     * @param parameters the parameters
     * @return the http response
     */
    public static HttpResponse executePost(final String url,
                                           final HttpEntity entity,
                                           final Map<String, String> parameters) {
        try {
            final HttpClient client = buildHttpClient(null, null);
            final URI uri = buildHttpUri(url, parameters);
            final HttpPost request = new HttpPost(uri);
            request.setEntity(entity);
            return client.execute(request);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Execute post http response.
     *
     * @param url    the url
     * @param entity the entity
     * @return the http response
     */
    public static HttpResponse executePost(final String url, final HttpEntity entity) {
        return executePost(url, entity, new HashMap<>());
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
                                           final Map<String, String> parameters) {
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
                                           final Map<String, String> parameters) {
        try {
            final HttpClient client = buildHttpClient(basicAuthUsername, basicAuthPassword);
            final URI uri = buildHttpUri(url, parameters);
            final HttpPost request = new HttpPost(uri);
            final StringEntity entity = new StringEntity(jsonEntity, ContentType.APPLICATION_JSON);
            request.setEntity(entity);
            return client.execute(request);
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
            final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(basicAuthUsername, basicAuthPassword);
            provider.setCredentials(AuthScope.ANY, credentials);
            return builder.setDefaultCredentialsProvider(provider);
        }
        return builder;
    }
}
