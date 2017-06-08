package org.apereo.cas.adaptors.u2f.storage;

import com.google.common.cache.LoadingCache;
import com.yubico.u2f.data.DeviceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collection;
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

    private static final String SELECT_QUERY = "SELECT r from U2FJpaDeviceRegistration r ";

    @PersistenceContext(unitName = "u2fEntityManagerFactory")
    private EntityManager entityManager;

    public U2FJpaDeviceRepository(final LoadingCache<String, String> requestStorage) {
        super(requestStorage);
    }

    @Override
    public Collection<DeviceRegistration> getRegisteredDevices(final String username) {
        try {
            return this.entityManager.createQuery(
                    SELECT_QUERY.concat("where r.username = :username"), U2FJpaDeviceRegistration.class)
                    .setParameter("username", username)
                    .getResultList()
                    .stream()
                    .map(r -> DeviceRegistration.fromJson(r.getRecord()))
                    .collect(Collectors.toList());
        } catch (final NoResultException e) {
            LOGGER.debug("No device registration was found for [{}]", username);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    @Override
    public void registerDevice(final String username, final DeviceRegistration registration) {
        authenticateDevice(username, registration);
    }

    @Override
    public void authenticateDevice(final String username, final DeviceRegistration registration) {
        final U2FJpaDeviceRegistration jpa = new U2FJpaDeviceRegistration();
        jpa.setUsername(username);
        jpa.setRecord(registration.toJson());
        this.entityManager.merge(jpa);
    }

    @Override
    public boolean isDeviceRegisteredFor(final String username) {
        return !getRegisteredDevices(username).isEmpty();
    }
}
