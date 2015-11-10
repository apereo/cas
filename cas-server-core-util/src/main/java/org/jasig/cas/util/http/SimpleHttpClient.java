package org.jasig.cas.util.http;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.FutureRequestExecutionService;
import org.apache.http.impl.client.HttpRequestFutureTask;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

/**
 * Implementation of CAS {@link HttpClient}
 * which delegates requests to a {@link #httpClient} instance.
 *
 * @author Jerome Leleu
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.1
 */
final class SimpleHttpClient implements HttpClient, Serializable, DisposableBean {

    /** Unique Id for serialization. */
    private static final long serialVersionUID = -4949380008568071855L;

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleHttpClient.class);

    /** the acceptable codes supported by this client. */
    private final List<Integer> acceptableCodes;

    /** the HTTP client for this client. */
    private final CloseableHttpClient httpClient;

    /** the request executor service for this client. */
    private final FutureRequestExecutionService requestExecutorService;

    /**
     * Instantiates a new Simple HTTP client, based on the provided inputs.
     *
     * @param acceptableCodes the acceptable codes of the client
     * @param httpClient the HTTP client used by the client
     * @param requestExecutorService the request executor service used by the client
     */
    SimpleHttpClient(final List<Integer> acceptableCodes, final CloseableHttpClient httpClient,
            final FutureRequestExecutionService requestExecutorService) {
        this.acceptableCodes = ImmutableList.copyOf(acceptableCodes);
        this.httpClient = httpClient;
        this.requestExecutorService = requestExecutorService;
    }

    @Override
    public boolean sendMessageToEndPoint(final HttpMessage message) {
        Assert.notNull(this.httpClient);

        try {
            final HttpPost request = new HttpPost(message.getUrl().toURI());
            request.addHeader("Content-Type", message.getContentType());
            
            final StringEntity entity = new StringEntity(message.getMessage(), ContentType.create(message.getContentType()));
            request.setEntity(entity);

            final HttpRequestFutureTask<String> task = this.requestExecutorService.execute(request,
                    HttpClientContext.create(), new BasicResponseHandler());

            if (message.isAsynchronous()) {
                return true;
            }
            
            return StringUtils.isNotBlank(task.get());
        } catch (final RejectedExecutionException e) {
            LOGGER.warn(e.getMessage(), e);
            return false;
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
            return false;
        }
    }
        
    @Override
    public boolean isValidEndPoint(final String url) {
        try {
            final URL u = new URL(url);
            return isValidEndPoint(u);
        } catch (final MalformedURLException e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean isValidEndPoint(final URL url) {
        Assert.notNull(this.httpClient);

        HttpEntity entity = null;

        try (final CloseableHttpResponse response = this.httpClient.execute(new HttpGet(url.toURI()))) {
            final int responseCode = response.getStatusLine().getStatusCode();

            for (final int acceptableCode : this.acceptableCodes) {
                if (responseCode == acceptableCode) {
                    LOGGER.debug("Response code from server matched {}.", responseCode);
                    return true;
                }
            }

            LOGGER.debug("Response code did not match any of the acceptable response codes. Code returned was {}",
                    responseCode);

            if (responseCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                final String value = response.getStatusLine().getReasonPhrase();
                LOGGER.error("There was an error contacting the endpoint: {}; The error was:\n{}", url.toExternalForm(),
                        value);
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
     * @throws Exception if the executor cannot properly shut down
     */
    @Override
    public void destroy() throws Exception {
        IOUtils.closeQuietly(this.requestExecutorService);
    }

}
