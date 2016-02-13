package org.jasig.cas.support.events.mongo;

import org.jasig.cas.support.events.AbstractCasEvent;
import org.jasig.cas.support.events.dao.CasEventsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.Collection;

/**
 * This is {@link MongoDbCasEventsRepository} that stores event data into a mongodb database.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Repository("casEventsRepository")
public class MongoDbCasEventsRepository implements CasEventsRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbCasEventsRepository.class);

    private static final String MONGODB_COLLECTION_NAME = MongoDbCasEventsRepository.class.getSimpleName();

    private String collectionName = MONGODB_COLLECTION_NAME;

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
            LOGGER.debug("Dropping database collection: {}", this.collectionName);
            this.mongoTemplate.dropCollection(this.collectionName);
        }

        if (!this.mongoTemplate.collectionExists(this.collectionName)) {
            LOGGER.debug("Creating database collection: {}", this.collectionName);
            this.mongoTemplate.createCollection(this.collectionName);
        }


    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    /**
     * Optionally, specify the name of the mongodb collection where services are to be kept.
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
    public void save(final AbstractCasEvent event) {

    }

    @Override
    public Collection<AbstractCasEvent> load() {
        return null;
    }
}
