package org.apereo.cas.services;

import org.apereo.cas.configuration.model.core.services.RestfulServiceRegistryProperties;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.hjson.JsonValue;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This is {@link RestfulServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class RestfulServiceRegistry extends AbstractServiceRegistry {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true)
        .build()
        .toObjectMapper();

    private final RestfulServiceRegistryProperties properties;

    public RestfulServiceRegistry(final ConfigurableApplicationContext applicationContext,
                                  final Collection<ServiceRegistryListener> serviceRegistryListeners,
                                  final RestfulServiceRegistryProperties properties) {
        super(applicationContext, serviceRegistryListeners);
        this.properties = properties;
    }

    private static Map<String, Object> getRequestHeaders() {
        val headers = new HashMap<String, Object>();
        headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.put("Accept", MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        HttpResponse response = null;
        try {
            invokeServiceRegistryListenerPreSave(registeredService);
            response = HttpUtils.executePost(properties.getUrl(), properties.getBasicAuthUsername(),
                properties.getBasicAuthPassword(), MAPPER.writeValueAsString(registeredService),
                Map.of(), getRequestHeaders());
            if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
                val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                return MAPPER.readValue(JsonValue.readHjson(result).toString(), RegisteredService.class);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return null;
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        HttpResponse response = null;
        try {
            val completeUrl = StringUtils.appendIfMissing(properties.getUrl(), "/")
                .concat(Long.toString(registeredService.getId()));
            invokeServiceRegistryListenerPreSave(registeredService);
            response = HttpUtils.executeDelete(completeUrl, properties.getBasicAuthUsername(),
                properties.getBasicAuthPassword(), Map.of(), getRequestHeaders());
            return response.getStatusLine().getStatusCode() == HttpStatus.OK.value();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return false;
    }

    @Override
    public Collection<RegisteredService> load() {
        HttpResponse response = null;
        try {
            response = HttpUtils.executeGet(properties.getUrl(), properties.getBasicAuthUsername(),
                properties.getBasicAuthPassword(), Map.of(), getRequestHeaders());
            if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
                val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                val services = (List<RegisteredService>) MAPPER.readValue(result, List.class);
                services.stream()
                    .map(this::invokeServiceRegistryListenerPostLoad)
                    .filter(Objects::nonNull)
                    .peek(s -> publishEvent(new CasRegisteredServiceLoadedEvent(this, s)));
                return services;
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return new ArrayList<>(0);
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        HttpResponse response = null;
        try {
            val completeUrl = StringUtils.appendIfMissing(properties.getUrl(), "/").concat(Long.toString(id));
            response = HttpUtils.executeGet(completeUrl, properties.getBasicAuthUsername(),
                properties.getBasicAuthPassword(), Map.of(), getRequestHeaders());
            if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
                val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                return MAPPER.readValue(result, RegisteredService.class);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return null;
    }

    @Override
    public long size() {
        return load().size();
    }
}
