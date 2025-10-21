package org.apereo.cas.authentication.surrogate;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServicePrincipalAccessStrategyEnforcer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.hjson.JsonValue;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * This is {@link SurrogateRestAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class SurrogateRestAuthenticationService extends BaseSurrogateAuthenticationService {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    public SurrogateRestAuthenticationService(final ServicesManager servicesManager,
                                              final CasConfigurationProperties casProperties,
                                              final RegisteredServicePrincipalAccessStrategyEnforcer principalAccessStrategyEnforcer,
                                              final ConfigurableApplicationContext applicationContext) {
        super(servicesManager, casProperties, principalAccessStrategyEnforcer, applicationContext);
    }

    @Override
    public boolean canImpersonateInternal(final String surrogate, final Principal principal,
                                          final Optional<? extends Service> service) {
        val properties = casProperties.getAuthn().getSurrogate().getRest();
        HttpResponse response = null;
        try {
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .maximumRetryAttempts(properties.getMaximumRetryAttempts())
                .method(HttpMethod.valueOf(properties.getMethod().toUpperCase(Locale.ENGLISH).trim()))
                .url(properties.getUrl())
                .headers(properties.getHeaders())
                .parameters(CollectionUtils.wrap("surrogate", surrogate, "principal", principal.getId()))
                .build();
            response = HttpUtils.execute(exec);
            val statusCode = response.getCode();
            return HttpStatus.valueOf(statusCode).is2xxSuccessful();
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public Collection<String> getImpersonationAccounts(final String username, final Optional<? extends Service> service) {
        val properties = casProperties.getAuthn().getSurrogate().getRest();
        HttpResponse response = null;
        try {
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .maximumRetryAttempts(properties.getMaximumRetryAttempts())
                .method(HttpMethod.valueOf(properties.getMethod().toUpperCase(Locale.ENGLISH).trim()))
                .url(properties.getUrl())
                .parameters(CollectionUtils.wrap("principal", username))
                .headers(properties.getHeaders())
                .build();
            response = HttpUtils.execute(exec);
            try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                val result = IOUtils.toString(content, StandardCharsets.UTF_8);
                val expectedType = MAPPER.getTypeFactory().constructParametricType(List.class, String.class);
                return MAPPER.readValue(JsonValue.readHjson(result).toString(), expectedType);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return new ArrayList<>();
    }
}
