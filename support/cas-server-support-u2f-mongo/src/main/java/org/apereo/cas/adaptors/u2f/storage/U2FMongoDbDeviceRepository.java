package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.util.DateTimeUtils;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.yubico.u2f.data.DeviceRegistration;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

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
                                      final String collectionName) {
        super(requestStorage);
        this.expirationTime = expirationTime;
        this.expirationTimeUnit = expirationTimeUnit;
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
    }

    @Override
    public Collection<? extends DeviceRegistration> getRegisteredDevices(final String username) {
        try {
            val expirationDate = LocalDate.now(ZoneId.systemDefault())
                .minus(this.expirationTime, DateTimeUtils.toChronoUnit(this.expirationTimeUnit));
            val query = new Query();
            query.addCriteria(Criteria.where("username").is(username).and("createdDate").gte(expirationDate));
            return this.mongoTemplate.find(query, U2FDeviceRegistration.class, this.collectionName)
                .stream()
                .map(r -> {
                    try {
                        return DeviceRegistration.fromJson(getCipherExecutor().decode(r.getRecord()));
                    } catch (final Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return new ArrayList<>(0);
    }

    @Override
    public void registerDevice(final String username, final DeviceRegistration registration) {
        val record = new U2FDeviceRegistration();
        record.setUsername(username);
        record.setRecord(getCipherExecutor().encode(registration.toJsonWithAttestationCert()));
        record.setCreatedDate(LocalDate.now(ZoneId.systemDefault()));
        this.mongoTemplate.save(record, this.collectionName);
    }

    @Override
    public boolean isDeviceRegisteredFor(final String username) {
        return !getRegisteredDevices(username).isEmpty();
    }

    @Override
    public void clean() {
        try {
            val expirationDate = LocalDate.now(ZoneId.systemDefault()).minus(this.expirationTime, DateTimeUtils.toChronoUnit(this.expirationTimeUnit));
            LOGGER.debug("Cleaning up expired U2F device registrations based on expiration date [{}]", expirationDate);

            val query = new Query();
            query.addCriteria(Criteria.where("createdDate").lte(expirationDate));
            this.mongoTemplate.remove(query, U2FDeviceRegistration.class, this.collectionName);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
    }

    @Override
    public void removeAll() {
        try {
            val query = new Query();
            query.addCriteria(Criteria.where("createdDate").exists(true));
            this.mongoTemplate.remove(query, U2FDeviceRegistration.class, this.collectionName);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
    }
}
