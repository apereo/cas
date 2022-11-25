package org.apereo.cas.util;

import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import javax.net.ssl.SSLHandshakeException;

import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link HttpUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@UtilityClass
public class HttpUtils {
    private static final int MAX_CONNECTIONS = 200;

    private static final int MAX_CONNECTIONS_PER_ROUTE = 20;

    private static final int CONNECT_TIMEOUT_IN_MILLISECONDS = 500;

    private static final int CONNECTION_REQUEST_TIMEOUT_IN_MILLISECONDS = 5 * 1000;

    private static final int SOCKET_TIMEOUT_IN_MILLISECONDS = 10 * 1000;

    /**
     * Execute http request and produce a response.
     *
     * @param execution the request
     * @return the http response
     */
    public HttpResponse execute(final HttpExecutionRequest execution) {
        val uri = buildHttpUri(execution.getUrl().trim(), execution.getParameters());
        val request = getHttpRequestByMethod(execution.getMethod().name().toLowerCase().trim(), execution.getEntity(), uri);
        try {
            val expressionResolver = SpringExpressionLanguageValueResolver.getInstance();
            execution.getHeaders().forEach((key, value) -> {
                val headerValue = expressionResolver.resolve(value);
                val headerKey = expressionResolver.resolve(key);
                request.addHeader(headerKey, headerValue);
            });
            prepareHttpRequest(request, execution);
            val client = getHttpClient(execution);
            return client.execute(request);
        } catch (final SSLHandshakeException e) {
            val sanitizedUrl = FunctionUtils.doUnchecked(
                () -> new URIBuilder(execution.getUrl()).removeQuery().clearParameters().build().toASCIIString());
            LoggingUtils.error(LOGGER, "SSL error accessing: [" + sanitizedUrl + ']', e);
            return new BasicHttpResponse(new BasicStatusLine(request.getProtocolVersion(),
                HttpStatus.SC_INTERNAL_SERVER_ERROR, sanitizedUrl));
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    private static CloseableHttpClient getHttpClient(final HttpExecutionRequest execution) throws Exception {
        val builder = getHttpClientBuilder(execution);
        if (StringUtils.isNotBlank(execution.getProxyUrl())) {
            val proxyEndpoint = new URL(execution.getProxyUrl());
            val proxy = new HttpHost(proxyEndpoint.getHost(), proxyEndpoint.getPort(), proxyEndpoint.getProtocol());
            builder.setProxy(proxy);
        }
        return builder.build();
    }

    /**
     * Close the response.
     *
     * @param response the response to close
     */
    public void close(final HttpResponse response) {
        if (response instanceof CloseableHttpResponse closeableHttpResponse) {
            try {
                closeableHttpResponse.close();
            } catch (final Exception e) {
                LoggingUtils.error(LOGGER, e);
            }
        }
    }

    /**
     * Create headers.
     *
     * @param basicAuthUser     the basic auth user
     * @param basicAuthPassword the basic auth password
     * @return http headers
     */
    public org.springframework.http.HttpHeaders createBasicAuthHeaders(final String basicAuthUser,
                                                                       final String basicAuthPassword) {
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
    public org.springframework.http.HttpHeaders createBasicAuthHeaders(final String basicAuthUser,
                                                                       final String basicAuthPassword,
                                                                       final String basicCharset) {
        val acceptHeaders = new org.springframework.http.HttpHeaders();
        acceptHeaders.setAccept(CollectionUtils.wrap(MediaType.APPLICATION_JSON));
        if (StringUtils.isNotBlank(basicAuthUser) && StringUtils.isNotBlank(basicAuthPassword)) {
            val authorization = basicAuthUser + ':' + basicAuthPassword;
            val basic = EncodingUtils.encodeBase64(authorization.getBytes(Charset.forName(basicCharset)));
            acceptHeaders.set(org.springframework.http.HttpHeaders.AUTHORIZATION, "Basic " + basic);
        }
        return acceptHeaders;
    }

    private HttpUriRequest getHttpRequestByMethod(final String method, final String entity, final URI uri) {
        if ("post".equalsIgnoreCase(method)) {
            val request = new HttpPost(uri);
            if (StringUtils.isNotBlank(entity)) {
                val stringEntity = new StringEntity(entity, StandardCharsets.UTF_8);
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
     * @param request   the request
     * @param execution the execution request
     */
    private void prepareHttpRequest(final HttpUriRequest request,
                                    final HttpExecutionRequest execution) {
        if (execution.isBasicAuthentication()) {
            val auth = EncodingUtils.encodeBase64(execution.getBasicAuthUsername() + ':' + execution.getBasicAuthPassword());
            request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + auth);
        }
        if (execution.isBearerAuthentication()) {
            request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + execution.getBearerToken());
        }
    }

    private URI buildHttpUri(final String url, final Map<String, String> parameters) {
        return FunctionUtils.doUnchecked(() -> {
            val uriBuilder = new URIBuilder(url);
            parameters.forEach(uriBuilder::addParameter);
            return uriBuilder.build();
        });
    }

    private HttpClientBuilder getHttpClientBuilder(final HttpExecutionRequest execution) {
        val requestConfig = RequestConfig.custom();
        requestConfig.setConnectTimeout(CONNECT_TIMEOUT_IN_MILLISECONDS);
        requestConfig.setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_IN_MILLISECONDS);
        requestConfig.setSocketTimeout(SOCKET_TIMEOUT_IN_MILLISECONDS);

        val builder = HttpClientBuilder
            .create()
            .useSystemProperties()
            .setMaxConnTotal(MAX_CONNECTIONS)
            .setMaxConnPerRoute(MAX_CONNECTIONS_PER_ROUTE)
            .setDefaultRequestConfig(requestConfig.build());
        Optional.ofNullable(execution.getHttpClient())
            .ifPresent(client -> {
                val httpClientFactory = client.httpClientFactory();
                builder.setSSLHostnameVerifier(httpClientFactory.getHostnameVerifier());
                builder.setSSLContext(httpClientFactory.getSslContext());
                builder.setSSLSocketFactory(httpClientFactory.getSslSocketFactory());
            });
        return builder;
    }

    @SuperBuilder
    @Getter
    public static class HttpExecutionRequest {
        private final HttpClient httpClient;

        @NonNull
        private final HttpMethod method;

        @NonNull
        private final String url;

        private final String basicAuthUsername;

        private final String basicAuthPassword;

        private final String entity;

        private final String proxyUrl;

        private final String bearerToken;

        @Builder.Default
        private final Map<String, String> parameters = new LinkedHashMap<>();

        @Builder.Default
        private final Map<String, String> headers = new LinkedHashMap<>();

        /**
         * Is basic authentication?
         *
         * @return true/false
         */
        private boolean isBasicAuthentication() {
            return StringUtils.isNotBlank(basicAuthUsername) && StringUtils.isNotBlank(basicAuthPassword);
        }

        /**
         * Is bearer authentication?
         *
         * @return true/false
         */
        private boolean isBearerAuthentication() {
            return StringUtils.isNotBlank(bearerToken);
        }
    }
}
