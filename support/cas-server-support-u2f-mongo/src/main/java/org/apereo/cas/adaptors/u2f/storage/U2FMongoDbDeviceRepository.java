package org.apereo.cas.adaptors.u2f.storage;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.yubico.u2f.data.DeviceRegistration;
import org.apereo.cas.util.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This is {@link U2FMongoDbDeviceRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class U2FMongoDbDeviceRepository extends BaseU2FDeviceRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(U2FMongoDbDeviceRepository.class);

    private final MongoTemplate mongoTemplate;
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
    public Collection<DeviceRegistration> getRegisteredDevices(final String username) {
        try {
            final LocalDate expirationDate = LocalDate.now().minus(this.expirationTime, DateTimeUtils.toChronoUnit(this.expirationTimeUnit));
            final Query query = new Query();
            query.addCriteria(Criteria.where("username").is(username).and("createdDate").gte(expirationDate));
            return this.mongoTemplate.find(query, U2FDeviceRegistration.class, this.collectionName)
                    .stream()
                    .map(r -> DeviceRegistration.fromJson(r.getRecord()))
                    .collect(Collectors.toList());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ArrayList<>(0);
    }

    @Override
    public void registerDevice(final String username, final DeviceRegistration registration) {
        authenticateDevice(username, registration);
    }

    @Override
    public void authenticateDevice(final String username, final DeviceRegistration registration) {
        final U2FDeviceRegistration record = new U2FDeviceRegistration();
        record.setUsername(username);
        record.setRecord(registration.toJson());
        record.setCreatedDate(LocalDate.now());
        this.mongoTemplate.save(record, this.collectionName);
    }

    @Override
    public boolean isDeviceRegisteredFor(final String username) {
        return !getRegisteredDevices(username).isEmpty();
    }

    @Override
    public void clean() {
        try {
            final LocalDate expirationDate = LocalDate.now().minus(this.expirationTime, DateTimeUtils.toChronoUnit(this.expirationTimeUnit));
            LOGGER.debug("Cleaning up expired U2F device registrations based on expiration date [{}]", expirationDate);

            final Query query = new Query();
            query.addCriteria(Criteria.where("createdDate").lte(expirationDate));
            this.mongoTemplate.remove(query, U2FDeviceRegistration.class, this.collectionName);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
