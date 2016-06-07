package org.apereo.cas.support.events.mongo;

import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.dao.AbstractCasEventRepository;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.Assert;

import java.util.Collection;

/**
 * This is {@link MongoDbCasEventRepository} that stores event data into a mongodb database.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class MongoDbCasEventRepository extends AbstractCasEventRepository {

    private String collectionName;

    private boolean dropCollection;

    private MongoOperations mongoTemplate;

    public MongoDbCasEventRepository(final MongoOperations mongoTemplate, final String collectionName, final boolean dropCollection) {
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
        this.dropCollection = dropCollection;

        Assert.notNull(this.mongoTemplate);
        if (this.dropCollection) {
            logger.debug("Dropping database collection: {}", this.collectionName);
            this.mongoTemplate.dropCollection(this.collectionName);
        }
        if (!this.mongoTemplate.collectionExists(this.collectionName)) {
            logger.debug("Creating database collection: {}", this.collectionName);
            this.mongoTemplate.createCollection(this.collectionName);
        }
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
    public Collection<CasEvent> getEventsForPrincipal(final String id) {
        final Query query = new Query();
        query.addCriteria(Criteria.where("principalId").is(id));
        return this.mongoTemplate.find(query, CasEvent.class, this.collectionName);
    }
}
