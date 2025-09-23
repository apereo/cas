package org.apereo.cas.util.http;

import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.HttpMessage;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.FutureRequestExecutionService;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.http.HttpHeaders;
import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

/**
 * Implementation of CAS {@link HttpClient}
 * which delegates requests to a {@link #wrappedHttpClient} instance.
 *
 * @author Jerome Leleu
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.1
 */
@Slf4j
public record SimpleHttpClient(List<Integer> acceptableCodes, CloseableHttpClient wrappedHttpClient, FutureRequestExecutionService requestExecutorService,
                               SimpleHttpClientFactoryBean httpClientFactory) implements HttpClient, Serializable, DisposableBean {

    @Serial
    private static final long serialVersionUID = -4949380008568071855L;

    @Override
    public boolean sendMessageToEndPoint(final HttpMessage message) {
        try {
            val request = new HttpPost(message.getUrl().toURI());
            request.addHeader(HttpHeaders.CONTENT_TYPE, message.getContentType());
            val entity = new StringEntity(message.getMessage(), ContentType.create(message.getContentType()));
            request.setEntity(entity);

            val handler = (HttpClientResponseHandler<Boolean>) response -> response.getCode() == HttpStatus.SC_OK;
            LOGGER.trace("Created HTTP post message payload [{}]", request);
            val task = this.requestExecutorService.execute(request, HttpClientContext.create(), handler);
            return message.isAsynchronous() || task.get();
        } catch (final RejectedExecutionException e) {
            LoggingUtils.warn(LOGGER, e);
            return false;
        } catch (final Exception e) {
            LOGGER.debug("Unable to send message", e);
            return false;
        }
    }

    @Override
    public HttpMessage sendMessageToEndPoint(final URL url) {
        try (val response = wrappedHttpClient.execute(new HttpGet(url.toURI()), HttpRequestUtils.HTTP_CLIENT_RESPONSE_HANDLER)) {
            val responseCode = response.getCode();

            for (val acceptableCode : this.acceptableCodes) {
                if (responseCode == acceptableCode) {
                    LOGGER.debug("Response code received from server matched [{}].", responseCode);
                    val entity = response.getEntity();
                    try {
                        val msg = new HttpMessage(url, IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8));
                        msg.setContentType(entity.getContentType());
                        msg.setResponseCode(responseCode);
                        return msg;
                    } finally {
                        EntityUtils.consumeQuietly(entity);
                    }
                }
            }
            LOGGER.warn("Response code [{}] from [{}] did not match any of the acceptable response codes.", responseCode, url);
            if (responseCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                val value = response.getReasonPhrase();
                LOGGER.error("There was an error contacting the endpoint: [{}]; The error:\n[{}]", url.toExternalForm(), value);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }

        return null;
    }

    @Override
    public boolean isValidEndPoint(final String url) {
        try {
            val u = new URI(url).toURL();
            return isValidEndPoint(u);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            return false;
        }
    }

    @Override
    public boolean isValidEndPoint(final URL url) {
        try (val response = wrappedHttpClient.execute(new HttpGet(url.toURI()), HttpRequestUtils.HTTP_CLIENT_RESPONSE_HANDLER)) {
            val responseCode = response.getCode();
            val idx = Collections.binarySearch(this.acceptableCodes, responseCode);
            if (idx >= 0) {
                LOGGER.debug("Response code from server matched [{}].", responseCode);
                return true;
            }

            LOGGER.debug("Response code did not match any of the acceptable response codes. Code returned was [{}]", responseCode);
            if (responseCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                val value = response.getReasonPhrase();
                LOGGER.error("There was an error contacting the endpoint: [{}]; The error was:\n[{}]", url.toExternalForm(), value);
            }

            val entity = response.getEntity();
            try {
                LOGGER.debug("Located entity with length [{}]", entity.getContentLength());
            } finally {
                EntityUtils.consumeQuietly(entity);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

    @Override
    public void destroy() {
        IOUtils.closeQuietly(this.wrappedHttpClient);
        IOUtils.closeQuietly(this.requestExecutorService);
        this.httpClientFactory.destroy();
    }

}
