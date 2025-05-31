package org.apereo.cas.services.query;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceIndexService;
import org.apereo.cas.services.ServicesManagerRegisteredServiceLocator;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.index.AttributeIndex;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * This is {@link DefaultRegisteredServiceIndexService}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultRegisteredServiceIndexService implements RegisteredServiceIndexService {
    private final List<ServicesManagerRegisteredServiceLocator> registeredServiceLocators;
    private final CasConfigurationProperties casProperties;
    private final IndexedCollection<RegisteredService> indexedRegisteredServices = new ConcurrentIndexedCollection<>();
    
    @Override
    public void clear() {
        if (isEnabled()) {
            indexedRegisteredServices.clear();
        }
    }

    @Override
    public int count() {
        return isEnabled()
            ? indexedRegisteredServices.size()
            : 0;
    }

    @Override
    public Optional<RegisteredService> findServiceBy(final long id) {
        return isEnabled()
            ? indexedRegisteredServices.stream().filter(registeredService -> registeredService.getId() == id).findFirst()
            : Optional.empty();
    }

    @Override
    public void initialize() {
        if (isEnabled()) {
            registeredServiceLocators.forEach(locator -> locator.getRegisteredServiceIndexes()
                .stream()
                .map(RegisteredServiceQueryIndex::getIndex)
                .filter(AttributeIndex.class::isInstance)
                .map(AttributeIndex.class::cast)
                .forEach(index -> {
                    LOGGER.debug("Adding registered service index [{}] supplied by [{}]",
                        index.getAttribute().toString(), locator.getClass().getSimpleName());
                    indexedRegisteredServices.addIndex(index);
                }));
        }
    }

    @Override
    public void indexServices(final Collection<RegisteredService> services) {
        if (isEnabled()) {
            indexedRegisteredServices.clear();
            indexedRegisteredServices.addAll(services);
        }
    }

    @Override
    public Stream<RegisteredService> findServiceBy(final RegisteredServiceQuery... queries) {
        if (isEnabled()) {
            val serviceQueries = Arrays
                .stream(queries)
                .map(RegisteredServiceQueryAttribute::new)
                .map(RegisteredServiceQueryAttribute::toQuery)
                .toList();

            if (serviceQueries.isEmpty()) {
                LOGGER.trace("No queries were provided to search for services");
                return Stream.empty();
            }

            if (serviceQueries.size() == 1) {
                try (val results = indexedRegisteredServices.retrieve(serviceQueries.getFirst())) {
                    return results.stream();
                }
            }
            val subQueries = serviceQueries.subList(2, serviceQueries.size());
            val query = QueryFactory.and(serviceQueries.getFirst(), serviceQueries.get(1), (Collection) subQueries);
            try (val results = indexedRegisteredServices.retrieve(query)) {
                return results.stream();
            }
        }
        return Stream.empty();
    }

    @Override
    public boolean isEnabled() {
        return casProperties.getServiceRegistry().getCore().isIndexServices();
    }

    @Override
    public void indexService(final RegisteredService service) {
        if (isEnabled()) {
            indexedRegisteredServices.removeIf(registeredService -> registeredService.getId() == service.getId());
            indexedRegisteredServices.add(service);
        }
    }

    @Override
    public boolean matches(final RegisteredService registeredService, final RegisteredServiceQuery query) {
        val queryAttribute = new RegisteredServiceQueryAttribute(query);
        val propertyValue = queryAttribute.getValue(registeredService, new QueryOptions());
        return query.getValue().equals(propertyValue);
    }

}

