package org.apereo.cas.interrupt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.interrupt.InterruptProperties;
import org.apereo.cas.services.RegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import java.net.URI;

/**
 * This is {@link RestEndpointInterruptInquirer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class RestEndpointInterruptInquirer extends BaseInterruptInquirer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestEndpointInterruptInquirer.class);
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
    private final InterruptProperties.Rest restProperties;

    public RestEndpointInterruptInquirer(final InterruptProperties.Rest restProperties) {
        this.restProperties = restProperties;
    }

    @Override
    public InterruptResponse inquire(final Authentication authentication, final RegisteredService registeredService, final Service service) {
        try {
            final HttpClientBuilder builder = HttpClientBuilder.create();

            if (StringUtils.isNotBlank(restProperties.getBasicAuthUsername()) && StringUtils.isNotBlank(restProperties.getBasicAuthPassword())) {
                final CredentialsProvider provider = new BasicCredentialsProvider();
                final UsernamePasswordCredentials credentials =
                        new UsernamePasswordCredentials(restProperties.getBasicAuthUsername(), restProperties.getBasicAuthPassword());
                provider.setCredentials(AuthScope.ANY, credentials);
                builder.setDefaultCredentialsProvider(provider);
            }
  
            final HttpClient client = builder.build();

            final URIBuilder uriBuilder = new URIBuilder(restProperties.getUrl());
            uriBuilder.addParameter("username", authentication.getPrincipal().getId());
            if (service != null) {
                uriBuilder.addParameter(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
            }
            final URI uri = uriBuilder.build();
            final HttpUriRequest request = restProperties.getMethod().equalsIgnoreCase(HttpMethod.GET.name()) ? new HttpGet(uri) : new HttpPost(uri);
            final HttpResponse response = client.execute(request);
            return MAPPER.readValue(response.getEntity().getContent(), InterruptResponse.class);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new InterruptResponse(false);
    }
}
