package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This is {@link U2FJpaDeviceRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableTransactionManagement
@Transactional(transactionManager = "transactionManagerU2f", propagation = Propagation.REQUIRED)
@Slf4j
public class U2FJpaDeviceRepository extends BaseU2FDeviceRepository {
    private static final String DELETE_QUERY = "DELETE from U2FJpaDeviceRegistration r ";

    private static final String SELECT_QUERY = "SELECT r from U2FJpaDeviceRegistration r ";

    private final long expirationTime;

    private final TimeUnit expirationTimeUnit;

    @PersistenceContext(unitName = "u2fEntityManagerFactory")
    private transient EntityManager entityManager;

    public U2FJpaDeviceRepository(final LoadingCache<String, String> requestStorage,
                                  final long expirationTime, final TimeUnit expirationTimeUnit,
                                  final CipherExecutor<Serializable, String> cipherExecutor) {
        super(requestStorage, cipherExecutor);
        this.expirationTime = expirationTime;
        this.expirationTimeUnit = expirationTimeUnit;
    }

    @Override
    public Collection<? extends U2FDeviceRegistration> getRegisteredDevices() {
        val expirationDate = LocalDate.now(ZoneId.systemDefault())
            .minus(this.expirationTime, DateTimeUtils.toChronoUnit(this.expirationTimeUnit));
        return entityManager.createQuery(SELECT_QUERY.concat("WHERE r.createdDate >= :expdate"), U2FJpaDeviceRegistration.class)
            .setParameter("expdate", expirationDate)
            .getResultList()
            .stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends U2FDeviceRegistration> getRegisteredDevices(final String username) {
        val expirationDate = LocalDate.now(ZoneId.systemDefault())
            .minus(this.expirationTime, DateTimeUtils.toChronoUnit(this.expirationTimeUnit));
        return this.entityManager.createQuery(
            SELECT_QUERY.concat("WHERE r.username = :username AND r.createdDate >= :expdate"),
            U2FJpaDeviceRegistration.class)
            .setParameter("username", username)
            .setParameter("expdate", expirationDate)
            .getResultList()
            .stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public U2FDeviceRegistration registerDevice(final U2FDeviceRegistration registration) {
        val jpa = U2FJpaDeviceRegistration.builder()
            .username(registration.getUsername())
            .record(registration.getRecord())
            .id(registration.getId())
            .build();
        return this.entityManager.merge(jpa);
    }

    @Override
    public boolean isDeviceRegisteredFor(final String username) {
        return !getRegisteredDevices(username).isEmpty();
    }

    @Override
    public void clean() {
        val expirationDate = LocalDate.now(ZoneId.systemDefault()).minus(expirationTime, DateTimeUtils.toChronoUnit(expirationTimeUnit));
        LOGGER.debug("Cleaning up expired U2F device registrations based on expiration date [{}]", expirationDate);
        val query = entityManager.createQuery(DELETE_QUERY.concat("WHERE r.createdDate <= :expdate"))
            .setParameter("expdate", expirationDate);
        query.executeUpdate();
    }

    @Override
    public void deleteRegisteredDevice(final U2FDeviceRegistration registration) {
        val query = entityManager.createQuery(DELETE_QUERY.concat("WHERE r.username <= :username AND r.id=:id"))
            .setParameter("username", registration.getUsername())
            .setParameter("id", registration.getId());
        query.executeUpdate();
    }

    @Override
    public void removeAll() {
        this.entityManager.createQuery(DELETE_QUERY).executeUpdate();
    }
}
