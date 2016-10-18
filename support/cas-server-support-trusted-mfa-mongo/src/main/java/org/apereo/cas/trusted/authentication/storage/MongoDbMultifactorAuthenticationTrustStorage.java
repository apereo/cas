package org.apereo.cas.trusted.authentication.storage;

import com.google.common.collect.Sets;
import com.mongodb.WriteResult;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.persistence.NoResultException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * This is {@link MongoDbMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class MongoDbMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {

    private String collectionName;

    private boolean dropCollection;

    private MongoOperations mongoTemplate;

    /**
     * Instantiates a new Mongo db multifactor authentication trust storage.
     *
     * @param collectionName the collection name
     * @param dropCollection the drop collection
     * @param mongoTemplate  the mongo template
     */
    public MongoDbMultifactorAuthenticationTrustStorage(final String collectionName, final boolean dropCollection,
                                                        final MongoOperations mongoTemplate) {
        this.collectionName = collectionName;
        this.dropCollection = dropCollection;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Initialize registry post construction.
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
    public void expire(final String key) {
        try {
            final Query query = new Query();
            query.addCriteria(Criteria.where("key").is(key));
            final WriteResult res = this.mongoTemplate.remove(query, MultifactorAuthenticationTrustRecord.class, this.collectionName);
            logger.info("Found and removed {} records", res.getN());
        } catch (final NoResultException e) {
            logger.info("No trusted authentication records could be found");
        }
    }

    @Override
    public void expire(final LocalDate onOrBefore) {
        try {
            final Query query = new Query();
            query.addCriteria(Criteria.where("date").lte(onOrBefore));
            final WriteResult res = this.mongoTemplate.remove(query, MultifactorAuthenticationTrustRecord.class, this.collectionName);
            logger.info("Found and removed {} records", res.getN());
        } catch (final NoResultException e) {
            logger.info("No trusted authentication records could be found");
        }
    }

    @Override
    public Set<MultifactorAuthenticationTrustRecord> get(final LocalDate onOrAfterDate) {
        final Query query = new Query();
        query.addCriteria(Criteria.where("date").gte(onOrAfterDate));
        final List<MultifactorAuthenticationTrustRecord> results =
                this.mongoTemplate.find(query, MultifactorAuthenticationTrustRecord.class, this.collectionName);
        return Sets.newHashSet(results);
    }

    @Override
    public Set<MultifactorAuthenticationTrustRecord> get(final String principal) {
        final Query query = new Query();
        query.addCriteria(Criteria.where("principal").is(principal));
        final List<MultifactorAuthenticationTrustRecord> results =
                this.mongoTemplate.find(query, MultifactorAuthenticationTrustRecord.class, this.collectionName);
        return Sets.newHashSet(results);
    }

    @Override
    protected MultifactorAuthenticationTrustRecord setInternal(final MultifactorAuthenticationTrustRecord record) {
        this.mongoTemplate.save(record, this.collectionName);
        return record;
    }
}
