package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.api.MutablePropertySource;
import org.apereo.cas.configuration.model.core.config.cloud.SpringCloudConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.jspecify.annotations.Nullable;
import org.slf4j.event.Level;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link RestfulPropertySource}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Slf4j
@SuppressWarnings("NullAway.Init")
public class RestfulPropertySource extends EnumerablePropertySource<Environment>
    implements MutablePropertySource<Environment> {

    /**
     * Configuration key prefix.
     */
    public static final String CAS_CONFIGURATION_PREFIX = "cas.spring.cloud.rest";

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final SpringCloudConfigurationProperties.Rest config;
    private Set<String> propertyNames;

    public RestfulPropertySource(final String context, final SpringCloudConfigurationProperties.Rest config) {
        super(context);
        this.config = config;
        refresh();
    }

    @Override
    public void refresh() {
        this.propertyNames = fetchPropertyNames();
    }

    @Override
    public void removeAll() {
        HttpResponse response = null;
        try {
            val executionRequest = executionRequestBuilder()
                .method(HttpMethod.DELETE)
                .build();
            response = HttpUtils.execute(executionRequest);
            val status = response != null ? HttpStatus.valueOf(response.getCode()) : HttpStatus.INTERNAL_SERVER_ERROR;
            LOGGER.debug("Removing all properties [{}] resulted in HTTP status [{}]", name, status);
            if (status.is2xxSuccessful()) {
                propertyNames.clear();
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public void removeProperty(final String name) {
        HttpResponse response = null;
        try {
            val executionRequest = executionRequestBuilder()
                .parameters(Map.of("name", name))
                .method(HttpMethod.DELETE)
                .build();
            response = HttpUtils.execute(executionRequest);
            val status = response != null ? HttpStatus.valueOf(response.getCode()) : HttpStatus.INTERNAL_SERVER_ERROR;
            LOGGER.debug("Removing property [{}] resulted in HTTP status [{}]", name, status);
            if (status.is2xxSuccessful()) {
                propertyNames.remove(name);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public MutablePropertySource setProperty(final String name, final Object value) {
        HttpResponse response = null;
        try {
            val executionRequest = executionRequestBuilder()
                .parameters(Map.of("name", name, "value", value))
                .method(HttpMethod.POST)
                .build();
            response = HttpUtils.execute(executionRequest);
            if (response != null) {
                val status = HttpStatus.valueOf(response.getCode());
                val logLevel = status.is2xxSuccessful() ? Level.INFO : Level.WARN;
                LOGGER.atLevel(logLevel).log("Updating property [{}] resulted in HTTP status [{}]", name, status);
                propertyNames.add(name);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return this;
    }

    @Override
    public String[] getPropertyNames() {
        return propertyNames.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    @Override
    public @Nullable Object getProperty(final String name) {
        if (propertyNames.contains(name)) {
            HttpResponse response = null;
            try {
                val executionRequest = executionRequestBuilder()
                    .parameters(Map.of("name", name))
                    .build();
                response = HttpUtils.execute(executionRequest);
                if (response instanceof final HttpEntityContainer container
                    && container.getEntity() != null
                    && Objects.requireNonNull(HttpStatus.resolve(response.getCode())).is2xxSuccessful()) {
                    try (val content = container.getEntity().getContent()) {
                        val results = IOUtils.toString(content, StandardCharsets.UTF_8);
                        LOGGER.trace("Received response for property [{}] as [{}]", name, results);
                        return results;
                    }
                }
            } catch (final Exception e) {
                LoggingUtils.error(LOGGER, e);
            } finally {
                HttpUtils.close(response);
            }
        }
        return null;
    }


    private HttpExecutionRequest.HttpExecutionRequestBuilder executionRequestBuilder() {
        val url = SpringExpressionLanguageValueResolver.getInstance().resolve(Objects.requireNonNull(config.getUrl(), "REST url for property source cannot be null"));
        val headers = CollectionUtils.<String, String>wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return HttpExecutionRequest.builder()
            .basicAuthPassword(config.getBasicAuthPassword())
            .basicAuthUsername(config.getBasicAuthUsername())
            .method(HttpMethod.GET)
            .url(url)
            .maximumRetryAttempts(0)
            .headers(headers);
    }


    private Set<String> fetchPropertyNames() {
        HttpResponse response = null;
        try {
            var executionRequest = executionRequestBuilder().build();
            executionRequest = executionRequest.withUrl(executionRequest.getUrl() + "/names");

            response = HttpUtils.execute(executionRequest);
            if (response instanceof final HttpEntityContainer container && container.getEntity() != null) {
                try (val content = container.getEntity().getContent()) {
                    val results = IOUtils.toString(content, StandardCharsets.UTF_8);
                    if (StringUtils.isNotBlank(results)) {
                        return MAPPER.readValue(results, new TypeReference<Set<String>>() {
                        });
                    }
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return new HashSet<>();
    }
}
