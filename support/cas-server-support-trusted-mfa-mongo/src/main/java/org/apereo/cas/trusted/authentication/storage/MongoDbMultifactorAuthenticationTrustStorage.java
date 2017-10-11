package org.apereo.cas.trusted.authentication.storage;

import com.mongodb.WriteResult;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is {@link MongoDbMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class MongoDbMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbMultifactorAuthenticationTrustStorage.class);
    
    private final String collectionName;
    private final MongoOperations mongoTemplate;

    /**
     * Instantiates a new Mongo db multifactor authentication trust storage.
     *
     * @param collectionName the collection name
     * @param mongoTemplate  the mongo template
     */
    public MongoDbMultifactorAuthenticationTrustStorage(final String collectionName, final MongoOperations mongoTemplate) {
        this.collectionName = collectionName;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void expire(final String key) {
        try {
            final Query query = new Query();
            query.addCriteria(Criteria.where("key").is(key));
            final WriteResult res = this.mongoTemplate.remove(query, MultifactorAuthenticationTrustRecord.class, this.collectionName);
            LOGGER.info("Found and removed [{}]", res.getN());
        } catch (final Exception e) {
            LOGGER.info("No trusted authentication records could be found");
        }
    }

    @Override
    public void expire(final LocalDate onOrBefore) {
        try {
            final Query query = new Query();
            query.addCriteria(Criteria.where("date").lte(onOrBefore));
            final WriteResult res = this.mongoTemplate.remove(query, MultifactorAuthenticationTrustRecord.class, this.collectionName);
            LOGGER.info("Found and removed [{}]", res.getN());
        } catch (final Exception e) {
            LOGGER.info("No trusted authentication records could be found");
        }
    }

    @Override
    public Set<MultifactorAuthenticationTrustRecord> get(final LocalDate onOrAfterDate) {
        final Query query = new Query();
        query.addCriteria(Criteria.where("date").gte(onOrAfterDate));
        final List<MultifactorAuthenticationTrustRecord> results =
                this.mongoTemplate.find(query, MultifactorAuthenticationTrustRecord.class, this.collectionName);
        return new HashSet<>(results);
    }

    @Override
    public Set<MultifactorAuthenticationTrustRecord> get(final String principal) {
        final Query query = new Query();
        query.addCriteria(Criteria.where("principal").is(principal));
        final List<MultifactorAuthenticationTrustRecord> results =
                this.mongoTemplate.find(query, MultifactorAuthenticationTrustRecord.class, this.collectionName);
        return new HashSet<>(results);
    }

    @Override
    protected MultifactorAuthenticationTrustRecord setInternal(final MultifactorAuthenticationTrustRecord record) {
        this.mongoTemplate.save(record, this.collectionName);
        return record;
    }
}
