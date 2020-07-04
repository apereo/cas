package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This is {@link BaseResourceU2FDeviceRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public abstract class BaseResourceU2FDeviceRepository extends BaseU2FDeviceRepository {
    /**
     * Key in the map that indicates list of devices.
     */
    public static final String MAP_KEY_DEVICES = "devices";

    private final long expirationTime;

    private final TimeUnit expirationTimeUnit;

    protected BaseResourceU2FDeviceRepository(final LoadingCache<String, String> requestStorage,
                                              final long expirationTime,
                                              final TimeUnit expirationTimeUnit,
                                              final CipherExecutor<Serializable, String> cipherExecutor) {
        super(requestStorage, cipherExecutor);
        this.expirationTime = expirationTime;
        this.expirationTimeUnit = expirationTimeUnit;
    }

    @Override
    public Collection<? extends U2FDeviceRegistration> getRegisteredDevices() {
        try {
            val devices = readDevicesFromResource();
            if (!devices.isEmpty()) {
                val devs = devices.get(MAP_KEY_DEVICES);
                val expirationDate = LocalDate.now(ZoneId.systemDefault()).minus(this.expirationTime,
                    DateTimeUtils.toChronoUnit(this.expirationTimeUnit));
                LOGGER.debug("Filtering devices for based on device expiration date [{}]", expirationDate);
                val list = devs
                    .stream()
                    .filter(d -> d.getCreatedDate().isAfter(expirationDate))
                    .collect(Collectors.toList());
                LOGGER.debug("There are [{}] device(s) remaining in repository", list.size());
                return list;
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return new ArrayList<>(0);
    }

    @Override
    public Collection<? extends U2FDeviceRegistration> getRegisteredDevices(final String username) {
        try {
            val devices = readDevicesFromResource();
            if (!devices.isEmpty()) {
                val devs = devices.get(MAP_KEY_DEVICES);
                val expirationDate = LocalDate.now(ZoneId.systemDefault()).minus(this.expirationTime,
                    DateTimeUtils.toChronoUnit(this.expirationTimeUnit));
                LOGGER.debug("Filtering devices for [{}] based on device expiration date [{}]", username, expirationDate);
                val list = devs
                    .stream()
                    .filter(d -> d.getUsername().equals(username) && d.getCreatedDate().isAfter(expirationDate))
                    .collect(Collectors.toList());
                LOGGER.debug("There are [{}] device(s) remaining in repository for [{}]", list.size(), username);
                return list;
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return new ArrayList<>(0);
    }

    @Override
    public U2FDeviceRegistration registerDevice(final U2FDeviceRegistration registration) {
        try {
            val devices = readDevicesFromResource();
            val list = new ArrayList<U2FDeviceRegistration>(0);

            if (!devices.isEmpty()) {
                val devs = devices.get(MAP_KEY_DEVICES);
                LOGGER.debug("Located [{}] devices in repository", devs.size());
                list.addAll(new ArrayList<>(devs));
            }
            list.add(registration);
            LOGGER.debug("There are [{}] device(s) remaining in repository. Storing...", list.size());
            writeDevicesBackToResource(list);
            return registration;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    @Override
    public boolean isDeviceRegisteredFor(final String username) {
        return !getRegisteredDevices(username).isEmpty();
    }

    @Override
    public void deleteRegisteredDevice(final U2FDeviceRegistration registration) {
        try {
            val devices = readDevicesFromResource();
            if (!devices.isEmpty()) {
                val list = new ArrayList<>(devices.get(MAP_KEY_DEVICES));
                LOGGER.debug("Located [{}] devices in repository", list.size());
                if (list.removeIf(d -> d.getId() == registration.getId() && d.getUsername().equals(registration.getUsername()))) {
                    LOGGER.debug("There are [{}] device(s) remaining in repository. Storing...", list.size());
                    writeDevicesBackToResource(list);
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
    }

    @Override
    public void clean() {
        try {
            val devices = readDevicesFromResource();
            if (!devices.isEmpty()) {
                val devs = devices.get(MAP_KEY_DEVICES);
                LOGGER.debug("Located [{}] devices in repository", devs.size());

                val expirationDate = LocalDate.now(ZoneId.systemDefault()).minus(this.expirationTime, DateTimeUtils.toChronoUnit(this.expirationTimeUnit));
                LOGGER.debug("Filtering devices based on device expiration date [{}]", expirationDate);
                val list = devs.stream()
                    .filter(d -> d.getCreatedDate().isAfter(expirationDate))
                    .collect(Collectors.toList());

                LOGGER.debug("There are [{}] device(s) remaining in repository. Storing...", list.size());
                writeDevicesBackToResource(list);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
    }

    /**
     * Read devices from resource map.
     *
     * @return the map
     * @throws Exception the exception
     */
    protected abstract Map<String, List<U2FDeviceRegistration>> readDevicesFromResource() throws Exception;

    /**
     * Write devices back to resource.
     * (It overrides  all devices saved before)
     *
     * @param list the list of devices to write
     * @throws Exception the exception
     */
    protected abstract void writeDevicesBackToResource(List<U2FDeviceRegistration> list) throws Exception;
}
