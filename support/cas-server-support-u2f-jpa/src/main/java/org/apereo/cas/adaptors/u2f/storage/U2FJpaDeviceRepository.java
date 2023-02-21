package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.transaction.support.TransactionOperations;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link U2FJpaDeviceRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class U2FJpaDeviceRepository extends BaseU2FDeviceRepository {
    private static final String DELETE_QUERY = "DELETE from U2FJpaDeviceRegistration r ";

    private static final String SELECT_QUERY = "SELECT r from U2FJpaDeviceRegistration r ";

    @PersistenceContext(unitName = "jpaU2fRegistryContext")
    private EntityManager entityManager;

    private final TransactionOperations transactionTemplate;

    public U2FJpaDeviceRepository(final LoadingCache<String, String> requestStorage,
                                  final CasConfigurationProperties casProperties,
                                  final CipherExecutor<Serializable, String> cipherExecutor,
                                  final TransactionOperations transactionTemplate) {
        super(casProperties, requestStorage, cipherExecutor);
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public Collection<? extends U2FDeviceRegistration> getRegisteredDevices() {
        return transactionTemplate.execute(status -> {
            val expirationDate = getDeviceExpiration();
            return entityManager.createQuery(SELECT_QUERY.concat("WHERE r.createdDate >= :expdate"), U2FJpaDeviceRegistration.class)
                .setParameter("expdate", expirationDate)
                .getResultList()
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        });
    }

    @Override
    public Collection<? extends U2FDeviceRegistration> getRegisteredDevices(final String username) {
        return transactionTemplate.execute(status -> {
            val expirationDate = getDeviceExpiration();
            return this.entityManager.createQuery(
                    SELECT_QUERY.concat("WHERE r.username = :username AND r.createdDate >= :expdate"),
                    U2FJpaDeviceRegistration.class)
                .setParameter("username", username)
                .setParameter("expdate", expirationDate)
                .getResultList()
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        });
    }

    @Override
    public U2FDeviceRegistration registerDevice(final U2FDeviceRegistration registration) {
        return transactionTemplate.execute(status -> {
            val jpa = U2FJpaDeviceRegistration.builder()
                .username(registration.getUsername())
                .record(registration.getRecord())
                .id(registration.getId())
                .build();
            return this.entityManager.merge(jpa);
        });
    }

    @Override
    public boolean isDeviceRegisteredFor(final String username) {
        return !getRegisteredDevices(username).isEmpty();
    }

    @Override
    public void clean() {
        transactionTemplate.executeWithoutResult(status -> {
            val expirationDate = getDeviceExpiration();
            LOGGER.debug("Cleaning up expired U2F device registrations based on expiration date [{}]", expirationDate);
            val query = entityManager.createQuery(DELETE_QUERY.concat("WHERE r.createdDate <= :expdate"))
                .setParameter("expdate", expirationDate);
            query.executeUpdate();
        });
    }

    @Override
    public void deleteRegisteredDevice(final U2FDeviceRegistration registration) {
        transactionTemplate.executeWithoutResult(status -> {
            val query = entityManager.createQuery(DELETE_QUERY.concat("WHERE r.username <= :username AND r.id=:id"))
                .setParameter("username", registration.getUsername())
                .setParameter("id", registration.getId());
            query.executeUpdate();
        });
    }

    @Override
    public void removeAll() {
        transactionTemplate.executeWithoutResult(status -> entityManager.createQuery(DELETE_QUERY).executeUpdate());
    }
}
