package org.apereo.cas.services;

import org.apereo.cas.configuration.model.core.services.RestfulServiceRegistryProperties;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.StringSerializer;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
    private final StringSerializer<RegisteredService> serializer;

    private final RestfulServiceRegistryProperties properties;

    public RestfulServiceRegistry(final ConfigurableApplicationContext applicationContext,
                                  final Collection<ServiceRegistryListener> serviceRegistryListeners,
                                  final RestfulServiceRegistryProperties properties) {
        super(applicationContext, serviceRegistryListeners);
        this.properties = properties;
        this.serializer = new RegisteredServiceJsonSerializer(applicationContext);
    }

    private static Map<String, String> getRequestHeaders(final RestfulServiceRegistryProperties properties) {
        val headers = new HashMap<String, String>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.putAll(properties.getHeaders());
        return headers;
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        HttpResponse response = null;
        try {
            registeredService.assignIdIfNecessary();
            invokeServiceRegistryListenerPreSave(registeredService);
            val entity = serializer.toString(registeredService);
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .method(HttpMethod.POST)
                .url(SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getUrl()))
                .headers(getRequestHeaders(properties))
                .entity(entity)
                .build();
            response = HttpUtils.execute(exec);
            if (response.getCode() == HttpStatus.OK.value()) {
                try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                    val result = IOUtils.toString(content, StandardCharsets.UTF_8);
                    return this.serializer.from(result);
                }
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
            val completeUrl = Strings.CI.appendIfMissing(
                    SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getUrl()), "/")
                .concat(Long.toString(registeredService.getId()));
            invokeServiceRegistryListenerPreSave(registeredService);
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .method(HttpMethod.DELETE)
                .url(completeUrl)
                .headers(getRequestHeaders(properties))
                .build();
            response = HttpUtils.execute(exec);
            return response.getCode() == HttpStatus.OK.value();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return false;
    }

    @Override
    public void deleteAll() {
        HttpResponse response = null;
        try {
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .method(HttpMethod.DELETE)
                .url(SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getUrl()))
                .headers(getRequestHeaders(properties))
                .build();
            response = HttpUtils.execute(exec);
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public Collection<RegisteredService> load() {
        HttpResponse response = null;
        val clientInfo = ClientInfoHolder.getClientInfo();
        try {
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getUrl()))
                .headers(getRequestHeaders(properties))
                .build();
            response = HttpUtils.execute(exec);
            if (response != null && response.getCode() == HttpStatus.OK.value()) {
                try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                    val result = IOUtils.toString(content, StandardCharsets.UTF_8);
                    val services = this.serializer.fromList(result);
                    services
                        .stream()
                        .map(this::invokeServiceRegistryListenerPostLoad)
                        .filter(Objects::nonNull)
                        .forEach(s -> publishEvent(new CasRegisteredServiceLoadedEvent(this, s, clientInfo)));
                    return services;
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return new ArrayList<>();
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        HttpResponse response = null;
        try {
            val completeUrl = Strings.CI.appendIfMissing(
                SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getUrl()), "/").concat(Long.toString(id));
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(completeUrl)
                .headers(getRequestHeaders(properties))
                .build();
            response = HttpUtils.execute(exec);
            if (response.getCode() == HttpStatus.OK.value()) {
                try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                    val result = IOUtils.toString(content, StandardCharsets.UTF_8);
                    return serializer.from(result);
                }
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
