package org.apereo.cas.util.http;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHttpResponse;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;
import org.springframework.http.MediaType;
import javax.net.ssl.SSLHandshakeException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
    private static final Timeout CONNECT_TIMEOUT_IN_MILLISECONDS = Timeout.ofMilliseconds(5000);
    private static final Timeout SOCKET_TIMEOUT_IN_MILLISECONDS = Timeout.ofMilliseconds(5000);
    private static final Timeout CONNECT_TTL_TIMEOUT_IN_MILLISECONDS = Timeout.ofMilliseconds(5000);
    private static final Timeout CONNECTION_REQUEST_TIMEOUT_IN_MILLISECONDS = Timeout.ofMilliseconds(5000);

    /**
     * Execute http request and produce a response.
     *
     * @param execution the request
     * @return the http response
     */
    public HttpResponse execute(final HttpExecutionRequest execution) {
        val uri = buildHttpUri(execution.getUrl().trim(), execution.getParameters());
        val request = getHttpRequestByMethod(execution.getMethod().name().toLowerCase(Locale.ENGLISH).trim(), execution.getEntity(), uri);
        try {
            val expressionResolver = SpringExpressionLanguageValueResolver.getInstance();
            execution.getHeaders().forEach((key, value) -> {
                val headerValue = expressionResolver.resolve(value);
                val headerKey = expressionResolver.resolve(key);
                request.addHeader(headerKey, headerValue);
            });
            prepareHttpRequest(request, execution);
            val client = getHttpClient(execution);
            return FunctionUtils.doAndRetry(retryContext -> {
                LOGGER.trace("Sending HTTP request to [{}]. Attempt: [{}]", request.getUri(), retryContext.getRetryCount());
                val res = client.execute(request);
                if (res == null || org.springframework.http.HttpStatus.valueOf(res.getCode()).isError()) {
                    val maxAttempts = (Integer) retryContext.getAttribute("retry.maxAttempts");
                    if (maxAttempts == null || retryContext.getRetryCount() != maxAttempts - 1) {
                        throw new IllegalStateException();
                    }
                }
                return res;
            }, execution.getMaximumRetryAttempts());
        } catch (final SSLHandshakeException e) {
            val sanitizedUrl = FunctionUtils.doUnchecked(
                () -> new URIBuilder(execution.getUrl()).removeQuery().clearParameters().build().toASCIIString());
            LoggingUtils.error(LOGGER, "SSL error accessing: [" + sanitizedUrl + ']', e);
            return new BasicHttpResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, sanitizedUrl);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    /**
     * Create http client.
     *
     * @param execution http execution request
     * @return http execution request
     */
    public static CloseableHttpClient getHttpClient(final HttpExecutionRequest execution) throws Exception {
        val builder = getHttpClientBuilder(execution);
        if (StringUtils.isNotBlank(execution.getProxyUrl())) {
            val proxyEndpoint = new URI(execution.getProxyUrl()).toURL();
            val proxy = new HttpHost(proxyEndpoint.getHost(), proxyEndpoint.getPort());
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
        if (response instanceof final CloseableHttpResponse closeableHttpResponse) {
            FunctionUtils.doAndHandle(__ -> closeableHttpResponse.close());
        }
    }

    /**
     * Create basic auth headers.
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
     * Create basic auth headers.
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

    /**
     * Create http request.
     *
     * @param method http request method
     * @param entity http request body
     * @param uri http request uri
     * @return http request with get method
     */
    public static HttpUriRequest getHttpRequestByMethod(final String method, final String entity, final URI uri) {
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
    public static void prepareHttpRequest(final HttpUriRequest request,
                                    final HttpExecutionRequest execution) {
        if (execution.isBasicAuthentication()) {
            val auth = EncodingUtils.encodeBase64(execution.getBasicAuthUsername() + ':' + execution.getBasicAuthPassword());
            request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + auth);
        }
        if (execution.isBearerAuthentication()) {
            request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + execution.getBearerToken());
        }
    }

    /**
     * Build URI for http request.
     *
     * @param url used for URI
     * @param parameters for URI
     * @return URI for http request
     */
    public static URI buildHttpUri(final String url, final Map<String, String> parameters) {
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

        val builder = HttpClientBuilder
            .create()
            .useSystemProperties()
            .setDefaultRequestConfig(requestConfig.build());
        if (execution.getMaximumRetryAttempts() <= 1) {
            builder.disableAutomaticRetries();
        }
        val socketFactory = Optional.ofNullable(execution.getHttpClient())
            .map(HttpClient::httpClientFactory)
            .filter(factory -> Objects.nonNull(factory.getSslSocketFactory()))
            .map(HttpClientFactory::getSslSocketFactory)
            .orElseGet(() -> getSslConnectionSocketFactory(execution));

        val connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
            .setSSLSocketFactory(socketFactory)
            .setDefaultSocketConfig(SocketConfig.custom()
                .setSoTimeout(SOCKET_TIMEOUT_IN_MILLISECONDS)
                .build())
            .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT)
            .setConnPoolPolicy(PoolReusePolicy.LIFO)
            .setDefaultConnectionConfig(ConnectionConfig.custom()
                .setTimeToLive(CONNECT_TTL_TIMEOUT_IN_MILLISECONDS)
                .setSocketTimeout(SOCKET_TIMEOUT_IN_MILLISECONDS)
                .setConnectTimeout(CONNECT_TIMEOUT_IN_MILLISECONDS)
                .build())
            .build();
        builder.setConnectionManager(connectionManager);
        return builder;
    }

    private static SSLConnectionSocketFactory getSslConnectionSocketFactory(final HttpExecutionRequest execution) {
        val builder = SSLConnectionSocketFactoryBuilder.create().useSystemProperties();
        Optional.ofNullable(execution.getHttpClient())
            .map(HttpClient::httpClientFactory)
            .ifPresentOrElse(factory -> {
                builder.setHostnameVerifier(factory.getHostnameVerifier());
                builder.setSslContext(factory.getSslContext());
            }, () -> builder.setSslContext(SSLContexts.createDefault()).setHostnameVerifier(new DefaultHostnameVerifier()));
        return builder.build();
    }

}
