package org.apereo.cas.trusted.authentication.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
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
            final var query = new Query();
            query.addCriteria(Criteria.where("recordKey").is(key));
            final var res = this.mongoTemplate.remove(query, MultifactorAuthenticationTrustRecord.class, this.collectionName);
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
            final var query = new Query();
            query.addCriteria(Criteria.where("recordDate").lte(onOrBefore));
            final var res = this.mongoTemplate.remove(query, MultifactorAuthenticationTrustRecord.class, this.collectionName);
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
    public Set<MultifactorAuthenticationTrustRecord> get(final LocalDateTime onOrAfterDate) {
        final var query = new Query();
        query.addCriteria(Criteria.where("recordDate").gte(onOrAfterDate));
        final var results = mongoTemplate.find(query, MultifactorAuthenticationTrustRecord.class, this.collectionName);
        return new HashSet<>(results);
    }

    @Override
    public Set<MultifactorAuthenticationTrustRecord> get(final String principal) {
        final var query = new Query();
        query.addCriteria(Criteria.where("principal").is(principal));
        final var results =
            this.mongoTemplate.find(query, MultifactorAuthenticationTrustRecord.class, this.collectionName);
        return new HashSet<>(results);
    }

    @Override
    protected MultifactorAuthenticationTrustRecord setInternal(final MultifactorAuthenticationTrustRecord record) {
        this.mongoTemplate.save(record, this.collectionName);
        return record;
    }
}
