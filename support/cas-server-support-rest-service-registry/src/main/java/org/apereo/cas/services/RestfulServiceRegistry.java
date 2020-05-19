package org.apereo.cas.services;

import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link RestfulServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class RestfulServiceRegistry extends AbstractServiceRegistry {
    private final transient RestOperations restTemplate;

    private final String url;

    private final MultiValueMap<String, String> headers;

    private final RegisteredServiceEntityMapper registeredServiceEntityMapper;

    public RestfulServiceRegistry(final ConfigurableApplicationContext applicationContext,
                                  final RestOperations restTemplate,
                                  final String url,
                                  final MultiValueMap<String, String> headers,
                                  final Collection<ServiceRegistryListener> serviceRegistryListeners,
                                  final RegisteredServiceEntityMapper registeredServiceEntityMapper) {
        super(applicationContext, serviceRegistryListeners);
        this.restTemplate = restTemplate;
        this.url = url;
        this.headers = headers;
        this.registeredServiceEntityMapper = registeredServiceEntityMapper;
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        try {
            invokeServiceRegistryListenerPreSave(registeredService);
            val result = registeredServiceEntityMapper.fromRegisteredService(registeredService);
            val requestEntity = new HttpEntity<Serializable>(result, this.headers);
            val responseEntity = restTemplate.exchange(this.url, HttpMethod.POST, requestEntity, Serializable.class);
            if (responseEntity.getStatusCode().is2xxSuccessful()) {
                return registeredServiceEntityMapper.toRegisteredService(responseEntity.getBody());
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        val result = registeredServiceEntityMapper.fromRegisteredService(registeredService);
        val responseEntity = restTemplate.exchange(this.url, HttpMethod.DELETE,
            new HttpEntity<>(result, this.headers), Integer.class);
        return responseEntity.getStatusCode().is2xxSuccessful();
    }

    @Override
    public Collection<RegisteredService> load() {
        val responseEntity = restTemplate.exchange(this.url, HttpMethod.GET,
            new HttpEntity<>(this.headers), Serializable[].class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            val results = responseEntity.getBody();
            if (results != null) {
                return Stream.of(results)
                    .map((Function<Serializable, RegisteredService>) registeredServiceEntityMapper::toRegisteredService)
                    .map(this::invokeServiceRegistryListenerPostLoad)
                    .filter(Objects::nonNull)
                    .peek(s -> publishEvent(new CasRegisteredServiceLoadedEvent(this, s)))
                    .collect(Collectors.toList());
            }
        }
        return new ArrayList<>(0);
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        val completeUrl = StringUtils.appendIfMissing(this.url, "/").concat(Long.toString(id));
        val responseEntity = restTemplate.exchange(completeUrl, HttpMethod.GET,
            new HttpEntity<>(id, this.headers), Serializable.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return registeredServiceEntityMapper.toRegisteredService(responseEntity.getBody());
        }
        return null;
    }

    @Override
    public long size() {
        return load().size();
    }
}
