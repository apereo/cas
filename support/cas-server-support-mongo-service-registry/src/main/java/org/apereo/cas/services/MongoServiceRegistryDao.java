package org.apereo.cas.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import java.util.List;
import java.util.regex.Pattern;
import lombok.ToString;

/**
 * <p>Implementation of {@code ServiceRegistryDao} that uses a MongoDb repository as the backend
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
@AllArgsConstructor
public class MongoServiceRegistryDao extends AbstractServiceRegistryDao {

    private final MongoOperations mongoTemplate;
    private final String collectionName;

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
    public RegisteredService findServiceById(final String id) {
        final Pattern pattern = Pattern.compile(id, Pattern.CASE_INSENSITIVE);
        return this.mongoTemplate.findOne(new Query(Criteria.where("serviceId").regex(pattern)), RegisteredService.class, this.collectionName);
    }

    @Override
    public List<RegisteredService> load() {
        final List<RegisteredService> list = this.mongoTemplate.findAll(RegisteredService.class, this.collectionName);
        list.stream().forEach(s -> publishEvent(new CasRegisteredServiceLoadedEvent(this, s)));
        return list;
    }

    @Override
    public RegisteredService save(final RegisteredService svc) {
        if (svc.getId() == AbstractRegisteredService.INITIAL_IDENTIFIER_VALUE) {
            ((AbstractRegisteredService) svc).setId(svc.hashCode());
        }
        this.mongoTemplate.save(svc, this.collectionName);
        LOGGER.debug("Saved registered service: [{}]", svc);
        return this.findServiceById(svc.getId());
    }

    @Override
    public long size() {
        return this.mongoTemplate.count(new Query(), RegisteredService.class, this.collectionName);
    }
}
