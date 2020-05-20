package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.util.DateTimeUtils;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.yubico.u2f.data.DeviceRegistration;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
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
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "transactionManagerU2f")
@Slf4j
public class U2FJpaDeviceRepository extends BaseU2FDeviceRepository {
    private static final String DELETE_QUERY = "DELETE from U2FJpaDeviceRegistration r ";

    private static final String SELECT_QUERY = "SELECT r from U2FJpaDeviceRegistration r ";

    private final long expirationTime;

    private final TimeUnit expirationTimeUnit;

    @PersistenceContext(unitName = "u2fEntityManagerFactory")
    private transient EntityManager entityManager;

    public U2FJpaDeviceRepository(final LoadingCache<String, String> requestStorage,
                                  final long expirationTime, final TimeUnit expirationTimeUnit) {
        super(requestStorage);
        this.expirationTime = expirationTime;
        this.expirationTimeUnit = expirationTimeUnit;
    }

    @Override
    public Collection<? extends DeviceRegistration> getRegisteredDevices(final String username) {
        try {
            val expirationDate = LocalDate.now(ZoneId.systemDefault())
                .minus(this.expirationTime, DateTimeUtils.toChronoUnit(this.expirationTimeUnit));
            return this.entityManager.createQuery(
                SELECT_QUERY.concat("where r.username = :username and r.createdDate >= :expdate"),
                U2FJpaDeviceRegistration.class)
                .setParameter("username", username)
                .setParameter("expdate", expirationDate)
                .getResultList()
                .stream()
                .map(r -> {
                    try {
                        return DeviceRegistration.fromJson(getCipherExecutor().decode(r.getRecord()));
                    } catch (final Exception e) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.error(e.getMessage(), e);
                        } else {
                            LOGGER.error(e.getMessage());
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } catch (final NoResultException e) {
            LOGGER.debug("No device registration was found for [{}]", username);
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
        val jpa = new U2FJpaDeviceRegistration();
        jpa.setUsername(username);
        jpa.setRecord(getCipherExecutor().encode(registration.toJsonWithAttestationCert()));
        jpa.setCreatedDate(LocalDate.now(ZoneId.systemDefault()));
        this.entityManager.merge(jpa);
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
            this.entityManager.createQuery(
                DELETE_QUERY.concat("where r.createdDate <= :expdate"))
                .setParameter("expdate", expirationDate)
                .executeUpdate();
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
            this.entityManager.createQuery(DELETE_QUERY).executeUpdate();
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
    }
}
