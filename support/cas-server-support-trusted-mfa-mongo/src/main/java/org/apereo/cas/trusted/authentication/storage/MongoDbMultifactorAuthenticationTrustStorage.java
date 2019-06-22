package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * This is {@link MongoDbMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class MongoDbMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {
    private final String collectionName;
    private final MongoOperations mongoTemplate;

    @Override
    public void expire(final String key) {
        try {
            val query = new Query();
            query.addCriteria(Criteria.where("recordKey").is(key));
            val res = this.mongoTemplate.remove(query, MultifactorAuthenticationTrustRecord.class, this.collectionName);
            LOGGER.info("Found and removed [{}]", res.getDeletedCount());
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(e.getMessage(), e);
            } else {
                LOGGER.info("No trusted authentication records could be found");
            }
        }
    }

    @Override
    public void expire(final LocalDateTime onOrBefore) {
        try {
            val query = new Query();
            query.addCriteria(Criteria.where("recordDate").lte(onOrBefore));
            val res = this.mongoTemplate.remove(query, MultifactorAuthenticationTrustRecord.class, this.collectionName);
            LOGGER.info("Found and removed [{}]", res.getDeletedCount());
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(e.getMessage(), e);
            } else {
                LOGGER.info("No trusted authentication records could be found");
            }
        }
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final LocalDateTime onOrAfterDate) {
        val query = new Query();
        query.addCriteria(Criteria.where("recordDate").gte(onOrAfterDate));
        val results = mongoTemplate.find(query, MultifactorAuthenticationTrustRecord.class, this.collectionName);
        return new HashSet<>(results);
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final String principal) {
        val query = new Query();
        query.addCriteria(Criteria.where("principal").is(principal));
        val results = mongoTemplate.find(query, MultifactorAuthenticationTrustRecord.class, this.collectionName);
        return new HashSet<>(results);
    }

    @Override
    public MultifactorAuthenticationTrustRecord get(final long id) {
        val query = new Query();
        query.addCriteria(Criteria.where("id").is(id));
        return mongoTemplate.findOne(query, MultifactorAuthenticationTrustRecord.class, this.collectionName);
    }

    @Override
    protected MultifactorAuthenticationTrustRecord setInternal(final MultifactorAuthenticationTrustRecord record) {
        this.mongoTemplate.save(record, this.collectionName);
        return record;
    }
}
