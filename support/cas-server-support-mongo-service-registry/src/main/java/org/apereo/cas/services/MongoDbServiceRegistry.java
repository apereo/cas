package org.apereo.cas.services;

import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>Implementation of {@code ServiceRegistry} that uses a MongoDb repository as the backend
 * persistence mechanism. The repository is configured by the Spring application context. </p>
 * <p>The class will automatically create a default collection to use with services. The name
 * of the collection may be specified.
 * It also presents the ability to drop an existing collection and start afresh.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
@ToString
public class MongoDbServiceRegistry extends AbstractServiceRegistry {

    private final MongoOperations mongoTemplate;
    private final String collectionName;

    public MongoDbServiceRegistry(final ConfigurableApplicationContext applicationContext,
                                  final MongoOperations mongoTemplate,
                                  final String collectionName,
                                  final Collection<ServiceRegistryListener> serviceRegistryListeners) {
        super(applicationContext, serviceRegistryListeners);
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
    }

    @Override
    public boolean delete(final RegisteredService svc) {
        if (this.findServiceById(svc.getId()) != null) {
            this.mongoTemplate.remove(svc, this.collectionName);
            LOGGER.debug("Removed registered service: [{}]", svc);
            return true;
        }
        return false;
    }

    @Override
    public RegisteredService findServiceById(final long svcId) {
        return this.mongoTemplate.findOne(new Query(Criteria.where("id").is(svcId)), RegisteredService.class, this.collectionName);
    }

    @Override
    public RegisteredService findServiceByExactServiceId(final String id) {
        return this.mongoTemplate.findOne(new Query(Criteria.where("serviceId").is(id)), RegisteredService.class, this.collectionName);
    }

    @Override
    public RegisteredService findServiceByExactServiceName(final String name) {
        return this.mongoTemplate.findOne(new Query(Criteria.where("name").is(name)), RegisteredService.class, this.collectionName);
    }

    @Override
    public Collection<RegisteredService> load() {
        val list = this.mongoTemplate.findAll(RegisteredService.class, this.collectionName);
        return list
            .stream()
            .map(this::invokeServiceRegistryListenerPostLoad)
            .filter(Objects::nonNull)
            .peek(s -> publishEvent(new CasRegisteredServiceLoadedEvent(this, s)))
            .collect(Collectors.toList());
    }

    @Override
    public RegisteredService save(final RegisteredService svc) {
        if (svc.getId() == AbstractRegisteredService.INITIAL_IDENTIFIER_VALUE) {
            svc.setId(svc.hashCode());
        }
        invokeServiceRegistryListenerPreSave(svc);
        this.mongoTemplate.save(svc, this.collectionName);
        LOGGER.debug("Saved registered service: [{}]", svc);
        return this.findServiceById(svc.getId());
    }

    @Override
    public long size() {
        return this.mongoTemplate.count(new Query(), RegisteredService.class, this.collectionName);
    }
}
