package org.apereo.cas.trusted.authentication.storage;

import com.mongodb.WriteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
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
            final Query query = new Query();
            query.addCriteria(Criteria.where("recordKey").is(key));
            final WriteResult res = this.mongoTemplate.remove(query, MultifactorAuthenticationTrustRecord.class, this.collectionName);
            LOGGER.info("Found and removed [{}]", res.getN());
        } catch (final Exception e) {
            LOGGER.info("No trusted authentication records could be found");
        }
    }

    @Override
    public void expire(final LocalDateTime onOrBefore) {
        try {
            final Query query = new Query();
            query.addCriteria(Criteria.where("recordDate").lte(onOrBefore));
            final WriteResult res = this.mongoTemplate.remove(query, MultifactorAuthenticationTrustRecord.class, this.collectionName);
            LOGGER.info("Found and removed [{}]", res.getN());
        } catch (final Exception e) {
            LOGGER.info("No trusted authentication records could be found");
        }
    }

    @Override
    public Set<MultifactorAuthenticationTrustRecord> get(final LocalDateTime onOrAfterDate) {
        final Query query = new Query();
        query.addCriteria(Criteria.where("recordDate").gte(onOrAfterDate));
        final List<MultifactorAuthenticationTrustRecord> results = mongoTemplate.find(query, MultifactorAuthenticationTrustRecord.class, this.collectionName);
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
