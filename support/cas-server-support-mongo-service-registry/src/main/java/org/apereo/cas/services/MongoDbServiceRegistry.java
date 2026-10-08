package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

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
        val query = new Query(Criteria.where("id").is(svc.getId()));
        val result = mongoTemplate.remove(query, RegisteredService.class, collectionName);
        LOGGER.debug("Removed [{}] registered service(s) with id [{}]", result.getDeletedCount(), svc.getId());
        return result.getDeletedCount() > 0;
    }

    @Override
    public void deleteAll() {
        this.mongoTemplate.remove(new Query(Criteria.where("serviceId").exists(true)), RegisteredService.class, this.collectionName);
    }

    @Override
    public @Nullable RegisteredService findServiceById(final long svcId) {
        return Optional.ofNullable(mongoTemplate.findOne(new Query(Criteria.where("id").is(svcId)), RegisteredService.class, collectionName))
            .map(this::invokeServiceRegistryListenerPostLoad)
            .orElse(null);
    }

    @Override
    public @Nullable RegisteredService findServiceByExactServiceId(final String id) {
        return StringUtils.isBlank(id) ? null : findFirst(Criteria.where("serviceId").is(id));
    }

    @Override
    public @Nullable RegisteredService findServiceByExactServiceName(final String name) {
        return findFirst(Criteria.where("name").is(name));
    }

    @Override
    public Collection<RegisteredService> load() {
        val list = mongoTemplate.findAll(RegisteredService.class, this.collectionName);
        val clientInfo = ClientInfoHolder.getClientInfo();
        return list
            .stream()
            .map(this::invokeServiceRegistryListenerPostLoad)
            .filter(Objects::nonNull)
            .peek(s -> publishEvent(new CasRegisteredServiceLoadedEvent(this, s, clientInfo)))
            .collect(Collectors.toList());
    }

    @Override
    public RegisteredService save(final RegisteredService svc) {
        svc.assignIdIfNecessary();
        invokeServiceRegistryListenerPreSave(svc);
        LOGGER.debug("Saving registered service: [{}]", svc);
        return mongoTemplate.save(svc, this.collectionName);
    }

    @Override
    public long size() {
        return mongoTemplate.estimatedCount(collectionName);
    }

    @Override
    public Stream<? extends RegisteredService> getServicesStream() {
        return mongoTemplate.stream(new Query(), RegisteredService.class, this.collectionName)
            .map(this::invokeServiceRegistryListenerPostLoad);
    }

    private @Nullable RegisteredService findFirst(final Criteria criteria) {
        return mongoTemplate.find(new Query(criteria), RegisteredService.class, collectionName)
            .stream()
            .sorted()
            .findFirst()
            .map(this::invokeServiceRegistryListenerPostLoad)
            .orElse(null);
    }
}
