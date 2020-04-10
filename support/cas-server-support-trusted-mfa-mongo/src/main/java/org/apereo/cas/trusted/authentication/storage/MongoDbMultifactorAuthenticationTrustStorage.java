package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.configuration.model.support.mfa.TrustedDevicesMultifactorProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * This is {@link MongoDbMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class MongoDbMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {
    private final MongoOperations mongoTemplate;

    public MongoDbMultifactorAuthenticationTrustStorage(final TrustedDevicesMultifactorProperties properties,
                                                        final CipherExecutor<Serializable, String> cipherExecutor,
                                                        final MongoOperations mongoTemplate,
                                                        final MultifactorAuthenticationTrustRecordKeyGenerator keyGenerationStrategy) {
        super(properties, cipherExecutor, keyGenerationStrategy);
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void remove(final String key) {
        try {
            val query = new Query();
            query.addCriteria(Criteria.where("recordKey").is(key));
            val res = this.mongoTemplate.remove(query,
                MultifactorAuthenticationTrustRecord.class,
                getTrustedDevicesMultifactorProperties().getMongo().getCollection());
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
    public void remove(final ZonedDateTime expirationDate) {
        try {
            val query = new Query();
            query.addCriteria(Criteria.where("expirationDate").lte(expirationDate));
            val res = this.mongoTemplate.remove(query, MultifactorAuthenticationTrustRecord.class,
                getTrustedDevicesMultifactorProperties().getMongo().getCollection());
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
    public Set<? extends MultifactorAuthenticationTrustRecord> getAll() {
        remove();
        val results = mongoTemplate.findAll(MultifactorAuthenticationTrustRecord.class,
            getTrustedDevicesMultifactorProperties().getMongo().getCollection());
        return new HashSet<>(results);
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final ZonedDateTime onOrAfterDate) {
        remove();
        val query = new Query();
        query.addCriteria(Criteria.where("recordDate").gte(onOrAfterDate));
        val results = mongoTemplate.find(query, MultifactorAuthenticationTrustRecord.class,
            getTrustedDevicesMultifactorProperties().getMongo().getCollection());
        return new HashSet<>(results);
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final String principal) {
        remove();
        val query = new Query();
        query.addCriteria(Criteria.where("principal").is(principal));
        val results = mongoTemplate.find(query, MultifactorAuthenticationTrustRecord.class,
            getTrustedDevicesMultifactorProperties().getMongo().getCollection());
        return new HashSet<>(results);
    }

    @Override
    public MultifactorAuthenticationTrustRecord get(final long id) {
        remove();
        val query = new Query();
        query.addCriteria(Criteria.where("id").is(id));
        return mongoTemplate.findOne(query, MultifactorAuthenticationTrustRecord.class,
            getTrustedDevicesMultifactorProperties().getMongo().getCollection());
    }

    @SneakyThrows
    @Override
    protected MultifactorAuthenticationTrustRecord saveInternal(final MultifactorAuthenticationTrustRecord record) {
        this.mongoTemplate.save(record, getTrustedDevicesMultifactorProperties().getMongo().getCollection());
        return record;
    }
}
