package org.apereo.cas.util.http;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.FutureRequestExecutionService;
import org.apache.http.impl.client.HttpRequestFutureTask;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Collectors;

/**
 * Implementation of CAS {@link HttpClient}
 * which delegates requests to a {@link #httpClient} instance.
 *
 * @author Jerome Leleu
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.1
 */
public class SimpleHttpClient implements HttpClient, Serializable, DisposableBean {

    /**
     * Unique Id for serialization.
     */
    private static final long serialVersionUID = -4949380008568071855L;

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleHttpClient.class);

    /**
     * the acceptable codes supported by this client.
     */
    private final List<Integer> acceptableCodes;

    /**
     * the HTTP client for this client.
     */
    private final transient CloseableHttpClient httpClient;

    /**
     * the request executor service for this client.
     */
    private final FutureRequestExecutionService requestExecutorService;

    /**
     * Instantiates a new Simple HTTP client, based on the provided inputs.
     *
     * @param acceptableCodes        the acceptable codes of the client
     * @param httpClient             the HTTP client used by the client
     * @param requestExecutorService the request executor service used by the client
     */
    SimpleHttpClient(final List<Integer> acceptableCodes, final CloseableHttpClient httpClient,
                     final FutureRequestExecutionService requestExecutorService) {
        this.acceptableCodes = acceptableCodes.stream().sorted().collect(Collectors.toList());
        this.httpClient = httpClient;
        this.requestExecutorService = requestExecutorService;
    }

    @Override
    public boolean sendMessageToEndPoint(final HttpMessage message) {
        try {
            final HttpPost request = new HttpPost(message.getUrl().toURI());
            request.addHeader("Content-Type", message.getContentType());

            final StringEntity entity = new StringEntity(message.getMessage(), ContentType.create(message.getContentType()));
            request.setEntity(entity);

            final ResponseHandler<Boolean> handler = response -> response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
            LOGGER.debug("Created HTTP post message payload [{}]", request);
            final HttpRequestFutureTask<Boolean> task = this.requestExecutorService.execute(request, HttpClientContext.create(), handler);
            if (message.isAsynchronous()) {
                return true;
            }
            return task.get();
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
        HttpEntity entity = null;

        try (CloseableHttpResponse response = this.httpClient.execute(new HttpGet(url.toURI()))) {
            final int responseCode = response.getStatusLine().getStatusCode();

            for (final int acceptableCode : this.acceptableCodes) {
                if (responseCode == acceptableCode) {
                    LOGGER.debug("Response code received from server matched [{}].", responseCode);
                    entity = response.getEntity();
                    final HttpMessage msg = new HttpMessage(url, IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8));
                    msg.setContentType(entity.getContentType().getValue());
                    msg.setResponseCode(responseCode);
                    return msg;
                }
            }
            LOGGER.warn("Response code [{}] from [{}] did not match any of the acceptable response codes.",
                    responseCode, url);
            if (responseCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                final String value = response.getStatusLine().getReasonPhrase();
                LOGGER.error("There was an error contacting the endpoint: [{}]; The error:\n[{}]", url.toExternalForm(),
                        value);
            }
        } catch (final Exception e) {
            LOGGER.error("Unable to send message", e);
        } finally {
            EntityUtils.consumeQuietly(entity);
        }
        return null;
    }

    @Override
    public boolean isValidEndPoint(final String url) {
        try {
            final URL u = new URL(url);
            return isValidEndPoint(u);
        } catch (final MalformedURLException e) {
            LOGGER.error("Unable to build URL", e);
            return false;
        }
    }

    @Override
    public boolean isValidEndPoint(final URL url) {
        HttpEntity entity = null;

        try (CloseableHttpResponse response = this.httpClient.execute(new HttpGet(url.toURI()))) {
            final int responseCode = response.getStatusLine().getStatusCode();

            final int idx = Collections.binarySearch(this.acceptableCodes, responseCode);
            if (idx >= 0) {
                LOGGER.debug("Response code from server matched [{}].", responseCode);
                return true;
            }

            LOGGER.debug("Response code did not match any of the acceptable response codes. Code returned was [{}]", responseCode);

            if (responseCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                final String value = response.getStatusLine().getReasonPhrase();
                LOGGER.error("There was an error contacting the endpoint: [{}]; The error was:\n[{}]", url.toExternalForm(), value);
            }

            entity = response.getEntity();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            EntityUtils.consumeQuietly(entity);
        }
        return false;
    }

    /**
     * Shutdown the executor service and close the http client.
     *
     */
    @Override
    public void destroy() {
        IOUtils.closeQuietly(this.requestExecutorService);
    }

    @Override
    public org.apache.http.client.HttpClient getWrappedHttpClient() {
        return this.httpClient;
    }
}
