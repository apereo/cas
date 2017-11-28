package org.apereo.cas.adaptors.u2f.storage;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.yubico.u2f.data.DeviceRegistration;
import org.apereo.cas.util.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
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
public class U2FJpaDeviceRepository extends BaseU2FDeviceRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(U2FJpaDeviceRepository.class);

    private static final String DELETE_QUERY = "DELETE from U2FDeviceRegistration r ";
    private static final String SELECT_QUERY = "SELECT r from U2FDeviceRegistration r ";

    @PersistenceContext(unitName = "u2fEntityManagerFactory")
    private EntityManager entityManager;

    private final long expirationTime;
    private final TimeUnit expirationTimeUnit;

    public U2FJpaDeviceRepository(final LoadingCache<String, String> requestStorage,
                                  final long expirationTime, final TimeUnit expirationTimeUnit) {
        super(requestStorage);
        this.expirationTime = expirationTime;
        this.expirationTimeUnit = expirationTimeUnit;
    }

    @Override
    public Collection<DeviceRegistration> getRegisteredDevices(final String username) {
        try {
            final LocalDate expirationDate = LocalDate.now().minus(this.expirationTime, DateTimeUtils.toChronoUnit(this.expirationTimeUnit));
            return this.entityManager.createQuery(
                    SELECT_QUERY.concat("where r.username = :username and r.createdDate >= :expdate"), U2FDeviceRegistration.class)
                    .setParameter("username", username)
                    .setParameter("expdate", expirationDate)
                    .getResultList()
                    .stream()
                    .map(r -> DeviceRegistration.fromJson(r.getRecord()))
                    .collect(Collectors.toList());
        } catch (final NoResultException e) {
            LOGGER.debug("No device registration was found for [{}]", username);
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
        final U2FDeviceRegistration jpa = new U2FDeviceRegistration();
        jpa.setUsername(username);
        jpa.setRecord(registration.toJson());
        jpa.setCreatedDate(LocalDate.now());
        this.entityManager.merge(jpa);
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
            this.entityManager.createQuery(
                    DELETE_QUERY.concat("where r.createdDate <= :expdate"))
                    .setParameter("expdate", expirationDate)
                    .executeUpdate();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
