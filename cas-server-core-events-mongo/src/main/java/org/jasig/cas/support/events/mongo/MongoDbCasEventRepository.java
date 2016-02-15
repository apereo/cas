package org.jasig.cas.support.events.mongo;

import org.jasig.cas.support.events.dao.AbstractCasEventRepository;
import org.jasig.cas.support.events.dao.CasEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * This is {@link MongoDbCasEventRepository} that stores event data into a mongodb database.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Repository("casEventRepository")
public class MongoDbCasEventRepository extends AbstractCasEventRepository {

    private static final String MONGODB_COLLECTION_NAME = "MongoDbCasEventRepository";

    @Value("${mongodb.events.collection:" + MONGODB_COLLECTION_NAME + '}')
    private String collectionName;

    @Value("${mongodb.events.dropcollection:false}")
    private boolean dropCollection;

    @Autowired
    @Qualifier("mongoEventsTemplate")
    @NotNull
    private MongoOperations mongoTemplate;


    /**
     * Initialized registry post construction.
     * Will decide if the configured collection should
     * be dropped and recreated.
     */
    @PostConstruct
    public void init() {
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

    /**
     * Optionally, specify the name of the mongodb collection where events are to be kept.
     * By default, the name of the collection is specified by the constant {@link #MONGODB_COLLECTION_NAME}
     * @param name the name
     */
    public void setCollectionName(final String name) {
        this.collectionName = name;
    }

    /**
     * When set to true, the collection will be dropped first before proceeding with other operations.
     * @param dropCollection the drop collection
     */
    public void setDropCollection(final boolean dropCollection) {
        this.dropCollection = dropCollection;
    }

    public void setMongoTemplate(final MongoOperations mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
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
        final Query query= new Query();
        query.addCriteria(Criteria.where("principalId").is(id));
        return this.mongoTemplate.find(query, CasEvent.class, this.collectionName);
    }
}
