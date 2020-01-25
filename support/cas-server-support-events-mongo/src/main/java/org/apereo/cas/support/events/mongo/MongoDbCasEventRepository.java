package org.apereo.cas.support.events.mongo;

import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.support.events.dao.AbstractCasEventRepository;
import org.apereo.cas.support.events.dao.CasEvent;

import lombok.ToString;
import lombok.val;
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
@ToString
public class MongoDbCasEventRepository extends AbstractCasEventRepository {

    private final MongoOperations mongoTemplate;

    private final String collectionName;

    public MongoDbCasEventRepository(final CasEventRepositoryFilter eventRepositoryFilter,
                                     final MongoOperations mongoTemplate,
                                     final String collectionName) {
        super(eventRepositoryFilter);
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
    }

    @Override
    public Collection<CasEvent> load() {
        return this.mongoTemplate.findAll(CasEvent.class, this.collectionName);
    }

    @Override
    public Collection<? extends CasEvent> load(final ZonedDateTime dateTime) {
        val query = new Query();
        query.addCriteria(Criteria.where(CREATION_TIME_PARAM).gte(dateTime.toString()));
        return this.mongoTemplate.find(query, CasEvent.class, this.collectionName);
    }

    @Override
    public Collection<? extends CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal) {
        val query = new Query();
        query.addCriteria(Criteria.where(TYPE_PARAM).is(type).and(PRINCIPAL_ID_PARAM).is(principal));
        return this.mongoTemplate.find(query, CasEvent.class, this.collectionName);
    }

    @Override
    public Collection<? extends CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal, final ZonedDateTime dateTime) {
        val query = new Query();
        query.addCriteria(Criteria.where(TYPE_PARAM).is(type).and(PRINCIPAL_ID_PARAM).is(principal).and(CREATION_TIME_PARAM).gte(dateTime.toString()));
        return this.mongoTemplate.find(query, CasEvent.class, this.collectionName);
    }

    @Override
    public Collection<? extends CasEvent> getEventsOfType(final String type) {
        val query = new Query();
        query.addCriteria(Criteria.where(TYPE_PARAM).is(type));
        return this.mongoTemplate.find(query, CasEvent.class, this.collectionName);
    }

    @Override
    public Collection<? extends CasEvent> getEventsOfType(final String type, final ZonedDateTime dateTime) {
        val query = new Query();
        query.addCriteria(Criteria.where(TYPE_PARAM).is(type).and(CREATION_TIME_PARAM).gte(dateTime.toString()));
        return this.mongoTemplate.find(query, CasEvent.class, this.collectionName);
    }

    @Override
    public Collection<? extends CasEvent> getEventsForPrincipal(final String id) {
        val query = new Query();
        query.addCriteria(Criteria.where(PRINCIPAL_ID_PARAM).is(id));
        return this.mongoTemplate.find(query, CasEvent.class, this.collectionName);
    }

    @Override
    public Collection<? extends CasEvent> getEventsForPrincipal(final String principal, final ZonedDateTime dateTime) {
        val query = new Query();
        query.addCriteria(Criteria.where(PRINCIPAL_ID_PARAM).is(principal).and(CREATION_TIME_PARAM).gte(dateTime.toString()));
        return this.mongoTemplate.find(query, CasEvent.class, this.collectionName);
    }

    @Override
    public void saveInternal(final CasEvent event) {
        this.mongoTemplate.save(event, this.collectionName);
    }
}
