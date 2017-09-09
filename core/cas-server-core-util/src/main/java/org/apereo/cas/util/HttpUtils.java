package org.apereo.cas.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.util.Map;

/**
 * This is {@link HttpUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public final class HttpUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

    private HttpUtils() {
    }

    /**
     * Execute http request and produce a response.
     *
     * @param url               the url
     * @param method            the method
     * @param basicAuthUsername the basic auth username
     * @param basicAuthPassword the basic auth password
     * @param parameters        the parameters
     * @return the http response
     */
    public static HttpResponse execute(final String url, final String method,
                                       final String basicAuthUsername,
                                       final String basicAuthPassword,
                                       final Map<String, String> parameters) {
        try {
            final HttpClientBuilder builder = HttpClientBuilder.create();
            prepareCredentialsIfNeeded(builder, basicAuthUsername, basicAuthPassword);

            final HttpClient client = builder.build();
            final URIBuilder uriBuilder = new URIBuilder(url);

            parameters.forEach(uriBuilder::addParameter);
            final URI uri = uriBuilder.build();
            final HttpUriRequest request = method.equalsIgnoreCase(HttpMethod.GET.name()) ? new HttpGet(uri) : new HttpPost(uri);
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
     */
    private static void prepareCredentialsIfNeeded(final HttpClientBuilder builder, final String basicAuthUsername, final String basicAuthPassword) {
        if (StringUtils.isNotBlank(basicAuthUsername) && StringUtils.isNotBlank(basicAuthPassword)) {
            final BasicCredentialsProvider provider = new BasicCredentialsProvider();
            final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(basicAuthUsername, basicAuthPassword);
            provider.setCredentials(AuthScope.ANY, credentials);
            builder.setDefaultCredentialsProvider(provider);
        }
    }
}
