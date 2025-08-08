package org.apereo.cas.util.http;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
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
import java.io.Serial;
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
    private static final Timeout CONNECT_TIMEOUT_IN_MILLISECONDS = getTimeout("connectionTimeout");
    private static final Timeout SOCKET_TIMEOUT_IN_MILLISECONDS = getTimeout("socketTimeout");
    private static final Timeout CONNECT_TTL_TIMEOUT_IN_MILLISECONDS = getTimeout("connectionTimeToLive");
    private static final Timeout CONNECTION_REQUEST_TIMEOUT_IN_MILLISECONDS = getTimeout("connectionRequest");

    private static Timeout getTimeout(final String setting) {
        val timeoutValue = StringUtils.defaultIfBlank(System.getProperty(HttpUtils.class.getName() + '.' + setting), "5000");
        return Timeout.ofMilliseconds(Long.parseLong(timeoutValue));
    }

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
                val res = client.execute(request, HttpRequestUtils.HTTP_CLIENT_RESPONSE_HANDLER);
                if (res == null || org.springframework.http.HttpStatus.valueOf(res.getCode()).isError()) {
                    val maxAttempts = (Integer) retryContext.getAttribute("retry.maxAttempts");
                    if (maxAttempts == null || retryContext.getRetryCount() != maxAttempts - 1) {
                        throw new HttpRequestExecutionException(res);
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
            if (e instanceof final HttpRequestExecutionException hre && hre.getResponse() != null) {
                val response = new BasicHttpResponse(hre.getResponse().getCode(), hre.getResponse().getReasonPhrase());
                response.setHeaders(hre.getResponse().getHeaders());
                return response;
            }
        }
        return null;
    }

    /**
     * Create http client.
     *
     * @param execution http execution request
     * @return http execution request
     */
    private static CloseableHttpClient getHttpClient(final HttpExecutionRequest execution) throws Exception {
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
     * @param uri    http request uri
     * @return http request with get method
     */
    private HttpUriRequest getHttpRequestByMethod(final String method, final String entity, final URI uri) {
        if ("post".equalsIgnoreCase(method)) {
            val request = new HttpPost(uri);
            if (StringUtils.isNotBlank(entity)) {
                val stringEntity = new StringEntity(entity, StandardCharsets.UTF_8);
                request.setEntity(stringEntity);
            }
            return request;
        } else if ("patch".equalsIgnoreCase(method)) {
            val request = new HttpPatch(uri);
            if (StringUtils.isNotBlank(entity)) {
                val stringEntity = new StringEntity(entity, StandardCharsets.UTF_8);
                request.setEntity(stringEntity);
            }
            return request;
        } else if ("delete".equalsIgnoreCase(method)) {
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

    /**
     * Build URI for http request.
     *
     * @param url        used for URI
     * @param parameters for URI
     * @return URI for http request
     */
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

    @RequiredArgsConstructor
    @Getter
    private static final class HttpRequestExecutionException extends IllegalStateException {
        @Serial
        private static final long serialVersionUID = 3764678971716744728L;

        private final HttpResponse response;
    }
}
