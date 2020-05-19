package org.apereo.cas.util.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.FutureRequestExecutionService;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.DisposableBean;

import java.io.Serializable;
import java.net.MalformedURLException;
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
@Getter
@RequiredArgsConstructor
public class SimpleHttpClient implements HttpClient, Serializable, DisposableBean {

    /**
     * Unique Id for serialization.
     */
    private static final long serialVersionUID = -4949380008568071855L;

    /**
     * the acceptable codes supported by this client.
     */
    private final List<Integer> acceptableCodes;

    /**
     * the HTTP client for this client.
     */
    private final transient CloseableHttpClient wrappedHttpClient;

    /**
     * the request executor service for this client.
     */
    private final FutureRequestExecutionService requestExecutorService;

    /**
     * The client factory that created and initialized this client instance.
     */
    private final SimpleHttpClientFactoryBean httpClientFactory;

    @Override
    public boolean sendMessageToEndPoint(final HttpMessage message) {
        try {
            val request = new HttpPost(message.getUrl().toURI());
            request.addHeader("Content-Type", message.getContentType());

            val entity = new StringEntity(message.getMessage(), ContentType.create(message.getContentType()));
            request.setEntity(entity);

            val handler = (ResponseHandler<Boolean>) response -> response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
            LOGGER.trace("Created HTTP post message payload [{}]", request);
            val task = this.requestExecutorService.execute(request, HttpClientContext.create(), handler);
            return message.isAsynchronous() || task.get();
        } catch (final RejectedExecutionException e) {
            LOGGER.warn("Execution rejected", e);
            return false;
        } catch (final Exception e) {
            LOGGER.debug("Unable to send message", e);
            return false;
        }
    }

    @Override
    public HttpMessage sendMessageToEndPoint(final URL url) {
        try (val response = this.wrappedHttpClient.execute(new HttpGet(url.toURI()))) {
            val responseCode = response.getStatusLine().getStatusCode();

            for (val acceptableCode : this.acceptableCodes) {
                if (responseCode == acceptableCode) {
                    LOGGER.debug("Response code received from server matched [{}].", responseCode);
                    val entity = response.getEntity();
                    try {
                        val msg = new HttpMessage(url, IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8));
                        msg.setContentType(entity.getContentType().getValue());
                        msg.setResponseCode(responseCode);
                        return msg;
                    } finally {
                        EntityUtils.consumeQuietly(entity);
                    }
                }
            }
            LOGGER.warn("Response code [{}] from [{}] did not match any of the acceptable response codes.", responseCode, url);
            if (responseCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                val value = response.getStatusLine().getReasonPhrase();
                LOGGER.error("There was an error contacting the endpoint: [{}]; The error:\n[{}]", url.toExternalForm(), value);
            }
        } catch (final Exception e) {
            LOGGER.error("Unable to send message", e);
        }

        return null;
    }

    @Override
    public boolean isValidEndPoint(final String url) {
        try {
            val u = new URL(url);
            return isValidEndPoint(u);
        } catch (final MalformedURLException e) {
            LOGGER.error("Unable to build URL", e);
            return false;
        }
    }

    @Override
    public boolean isValidEndPoint(final URL url) {
        try (val response = this.wrappedHttpClient.execute(new HttpGet(url.toURI()))) {
            val responseCode = response.getStatusLine().getStatusCode();
            val idx = Collections.binarySearch(this.acceptableCodes, responseCode);
            if (idx >= 0) {
                LOGGER.debug("Response code from server matched [{}].", responseCode);
                return true;
            }

            LOGGER.debug("Response code did not match any of the acceptable response codes. Code returned was [{}]", responseCode);

            if (responseCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                val value = response.getStatusLine().getReasonPhrase();
                LOGGER.error("There was an error contacting the endpoint: [{}]; The error was:\n[{}]", url.toExternalForm(), value);
            }

            val entity = response.getEntity();
            try {
                LOGGER.debug("Located entity with length [{}]", entity.getContentLength());
            } finally {
                EntityUtils.consumeQuietly(entity);
            }
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
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
