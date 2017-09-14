package org.apereo.cas.support.events.mongo;

import org.apereo.cas.support.events.dao.AbstractCasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.ZonedDateTime;
import java.util.Collection;

/**
 * This is {@link MongoDbCasEventRepository} that stores event data into a mongodb database.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class MongoDbCasEventRepository extends AbstractCasEventRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbCasEventRepository.class);

    private final String collectionName;
    private final MongoOperations mongoTemplate;

    public MongoDbCasEventRepository(final MongoOperations mongoTemplate, final String collectionName) {
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public void save(final CasEvent event) {
        this.mongoTemplate.save(event, this.collectionName);
    }

    @Override
    public Collection<CasEvent> load() {
        return this.mongoTemplate.findAll(CasEvent.class, this.collectionName);
    }

    @Override
    public Collection<CasEvent> load(final ZonedDateTime dateTime) {
        final Query query = new Query();
        query.addCriteria(Criteria.where(CREATION_TIME_PARAM).gte(dateTime.toString()));
        return this.mongoTemplate.find(query, CasEvent.class, this.collectionName);
    }

    @Override
    public Collection<CasEvent> getEventsOfType(final String type) {
        final Query query = new Query();
        query.addCriteria(Criteria.where(TYPE_PARAM).is(type));
        return this.mongoTemplate.find(query, CasEvent.class, this.collectionName);
    }

    @Override
    public Collection<CasEvent> getEventsOfType(final String type, final ZonedDateTime dateTime) {
        final Query query = new Query();
        query.addCriteria(Criteria.where(TYPE_PARAM).is(type).and(CREATION_TIME_PARAM).gte(dateTime.toString()));
        return this.mongoTemplate.find(query, CasEvent.class, this.collectionName);
    }
    
    @Override
    public Collection<CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal) {
        final Query query = new Query();
        query.addCriteria(Criteria.where(TYPE_PARAM).is(type).and(PRINCIPAL_ID_PARAM).is(principal));
        return this.mongoTemplate.find(query, CasEvent.class, this.collectionName);
    }

    @Override
    public Collection<CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal, final ZonedDateTime dateTime) {
        final Query query = new Query();
        query.addCriteria(Criteria.where(TYPE_PARAM).is(type).and(PRINCIPAL_ID_PARAM).is(principal).and(CREATION_TIME_PARAM).gte(dateTime.toString()));
        return this.mongoTemplate.find(query, CasEvent.class, this.collectionName);
    }

    @Override
    public Collection<CasEvent> getEventsForPrincipal(final String id) {
        final Query query = new Query();
        query.addCriteria(Criteria.where(PRINCIPAL_ID_PARAM).is(id));
        return this.mongoTemplate.find(query, CasEvent.class, this.collectionName);
    }

    @Override
    public Collection<CasEvent> getEventsForPrincipal(final String principal, final ZonedDateTime dateTime) {
        final Query query = new Query();
        query.addCriteria(Criteria.where(PRINCIPAL_ID_PARAM).is(principal).and(CREATION_TIME_PARAM).gte(dateTime.toString()));
        return this.mongoTemplate.find(query, CasEvent.class, this.collectionName);
    }
}
