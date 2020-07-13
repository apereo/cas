package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This is {@link U2FMongoDbDeviceRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class U2FMongoDbDeviceRepository extends BaseU2FDeviceRepository {

    private final transient MongoTemplate mongoTemplate;

    private final long expirationTime;

    private final TimeUnit expirationTimeUnit;

    private final String collectionName;

    public U2FMongoDbDeviceRepository(final LoadingCache<String, String> requestStorage,
                                      final MongoTemplate mongoTemplate,
                                      final long expirationTime,
                                      final TimeUnit expirationTimeUnit,
                                      final String collectionName,
                                      final CipherExecutor<Serializable, String> cipherExecutor) {
        super(requestStorage, cipherExecutor);
        this.expirationTime = expirationTime;
        this.expirationTimeUnit = expirationTimeUnit;
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
    }

    @Override
    public Collection<? extends U2FDeviceRegistration> getRegisteredDevices() {
        try {
            val expirationDate = LocalDate.now(ZoneId.systemDefault())
                .minus(this.expirationTime, DateTimeUtils.toChronoUnit(this.expirationTimeUnit));
            val query = new Query();
            query.addCriteria(Criteria.where("createdDate").gte(expirationDate));
            return queryDeviceRegistrations(query);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return new ArrayList<>(0);
    }

    @Override
    public Collection<? extends U2FDeviceRegistration> getRegisteredDevices(final String username) {
        try {
            val expirationDate = LocalDate.now(ZoneId.systemDefault())
                .minus(this.expirationTime, DateTimeUtils.toChronoUnit(this.expirationTimeUnit));
            val query = new Query();
            query.addCriteria(Criteria.where("username").is(username).and("createdDate").gte(expirationDate));
            return queryDeviceRegistrations(query);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return new ArrayList<>(0);
    }

    @Override
    public U2FDeviceRegistration registerDevice(final U2FDeviceRegistration registration) {
        return this.mongoTemplate.save(registration, this.collectionName);
    }

    @Override
    public boolean isDeviceRegisteredFor(final String username) {
        return !getRegisteredDevices(username).isEmpty();
    }

    @Override
    public void clean() {
        try {
            val expirationDate = LocalDate.now(ZoneId.systemDefault()).minus(this.expirationTime,
                DateTimeUtils.toChronoUnit(this.expirationTimeUnit));
            LOGGER.debug("Cleaning up expired U2F device registrations based on expiration date [{}]", expirationDate);

            val query = new Query();
            query.addCriteria(Criteria.where("createdDate").lte(expirationDate));
            this.mongoTemplate.remove(query, U2FDeviceRegistration.class, this.collectionName);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
    }

    @Override
    public void removeAll() {
        try {
            val query = new Query();
            query.addCriteria(Criteria.where("createdDate").exists(true));
            this.mongoTemplate.remove(query, U2FDeviceRegistration.class, this.collectionName);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
    }

    @Override
    public void deleteRegisteredDevice(final U2FDeviceRegistration record) {
        try {
            val query = new Query();
            query.addCriteria(Criteria.where("username").is(record.getUsername())
                .and("id").is(record.getId()));
            this.mongoTemplate.remove(query, U2FDeviceRegistration.class, this.collectionName);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
    }

    private Collection<? extends U2FDeviceRegistration> queryDeviceRegistrations(final Query query) {
        return this.mongoTemplate.find(query, U2FDeviceRegistration.class,
            this.collectionName)
            .stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
