package cas.authentication.surrogate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.surrogate.BaseSurrogateAuthenticationService;
import org.apereo.cas.configuration.model.support.surrogate.SurrogateAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link SurrogateRestAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SurrogateRestAuthenticationService extends BaseSurrogateAuthenticationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SurrogateRestAuthenticationService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .findAndRegisterModules()
            .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final SurrogateAuthenticationProperties.Rest properties;

    public SurrogateRestAuthenticationService(final SurrogateAuthenticationProperties.Rest properties,
                                              final ServicesManager servicesManager) {
        super(servicesManager);
        this.properties = properties;
    }
    

    /**
     * Prepare credentials if needed.
     *
     * @param builder the builder
     */
    protected void prepareCredentialsIfNeeded(final HttpClientBuilder builder) {
        if (StringUtils.isNotBlank(properties.getBasicAuthUsername())
                && StringUtils.isNotBlank(properties.getBasicAuthPassword())) {
            final BasicCredentialsProvider provider = new BasicCredentialsProvider();
            final UsernamePasswordCredentials credentials =
                    new UsernamePasswordCredentials(properties.getBasicAuthUsername(), properties.getBasicAuthPassword());
            provider.setCredentials(AuthScope.ANY, credentials);
            builder.setDefaultCredentialsProvider(provider);
        }
    }

    @Override
    public boolean canAuthenticateAsInternal(final String surrogate, final Principal principal) {
        try {
            final HttpClientBuilder builder = HttpClientBuilder.create();
            prepareCredentialsIfNeeded(builder);

            final HttpClient client = builder.build();
            final URIBuilder uriBuilder = new URIBuilder(properties.getUrl());
            uriBuilder.addParameter("surrogate", surrogate);
            uriBuilder.addParameter("principal", principal.getId());

            final URI uri = uriBuilder.build();
            final HttpUriRequest request = properties.getMethod().equalsIgnoreCase(HttpMethod.GET.name()) ? new HttpGet(uri) : new HttpPost(uri);
            final HttpResponse response = client.execute(request);
            return response.getStatusLine().getStatusCode() == HttpStatus.ACCEPTED.value();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public Collection<String> getEligibleAccountsForSurrogateToProxy(final String username) {
        try {
            final HttpClientBuilder builder = HttpClientBuilder.create();
            prepareCredentialsIfNeeded(builder);

            final HttpClient client = builder.build();
            final URIBuilder uriBuilder = new URIBuilder(properties.getUrl());
            uriBuilder.addParameter("principal", username);

            final URI uri = uriBuilder.build();
            final HttpUriRequest request = properties.getMethod().equalsIgnoreCase(HttpMethod.GET.name()) ? new HttpGet(uri) : new HttpPost(uri);
            final HttpResponse response = client.execute(request);
            return MAPPER.readValue(response.getEntity().getContent(), List.class);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ArrayList<>();
    }
}
