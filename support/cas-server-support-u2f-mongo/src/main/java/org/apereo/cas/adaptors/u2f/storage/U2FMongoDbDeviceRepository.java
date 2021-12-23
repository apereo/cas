package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
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

    public U2FMongoDbDeviceRepository(final LoadingCache<String, String> requestStorage,
                                      final MongoTemplate mongoTemplate,
                                      final CipherExecutor<Serializable, String> cipherExecutor,
                                      final CasConfigurationProperties casProperties) {
        super(casProperties, requestStorage, cipherExecutor);
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Collection<? extends U2FDeviceRegistration> getRegisteredDevices(final String username) {
        val expirationDate = getDeviceExpiration();
        val query = new Query();
        query.addCriteria(Criteria.where("username").is(username).and("createdDate").gte(expirationDate));
        return queryDeviceRegistrations(query);

    }

    @Override
    public Collection<? extends U2FDeviceRegistration> getRegisteredDevices() {
        val expirationDate = getDeviceExpiration();
        val query = new Query();
        query.addCriteria(Criteria.where("createdDate").gte(expirationDate));
        return queryDeviceRegistrations(query);
    }

    @Override
    public U2FDeviceRegistration registerDevice(final U2FDeviceRegistration registration) {
        return this.mongoTemplate.save(registration, casProperties.getAuthn().getMfa().getU2f().getMongo().getCollection());
    }

    @Override
    public void deleteRegisteredDevice(final U2FDeviceRegistration record) {
        val query = new Query();
        query.addCriteria(Criteria.where("username").is(record.getUsername())
            .and("id").is(record.getId()));
        this.mongoTemplate.remove(query, U2FDeviceRegistration.class,
            casProperties.getAuthn().getMfa().getU2f().getMongo().getCollection());
    }

    @Override
    public boolean isDeviceRegisteredFor(final String username) {
        return !getRegisteredDevices(username).isEmpty();
    }

    @Override
    public void clean() {
        val expirationDate = getDeviceExpiration();
        LOGGER.debug("Cleaning up expired U2F device registrations based on expiration date [{}]", expirationDate);

        val query = new Query();
        query.addCriteria(Criteria.where("createdDate").lte(expirationDate));
        this.mongoTemplate.remove(query, U2FDeviceRegistration.class,
            casProperties.getAuthn().getMfa().getU2f().getMongo().getCollection());
    }

    @Override
    public void removeAll() {
        val query = new Query();
        query.addCriteria(Criteria.where("createdDate").exists(true));
        mongoTemplate.remove(query, U2FDeviceRegistration.class,
            casProperties.getAuthn().getMfa().getU2f().getMongo().getCollection());
    }

    private Collection<? extends U2FDeviceRegistration> queryDeviceRegistrations(final Query query) {
        return mongoTemplate.find(query, U2FDeviceRegistration.class,
                casProperties.getAuthn().getMfa().getU2f().getMongo().getCollection())
            .stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
