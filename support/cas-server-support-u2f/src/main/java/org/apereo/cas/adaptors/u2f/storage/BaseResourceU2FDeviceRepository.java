package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.util.DateTimeUtils;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.yubico.u2f.data.DeviceRegistration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.mail.AuthenticationFailedException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    public BaseResourceU2FDeviceRepository(final LoadingCache<String, String> requestStorage,
                                           final long expirationTime, final TimeUnit expirationTimeUnit) {
        super(requestStorage);
        this.expirationTime = expirationTime;
        this.expirationTimeUnit = expirationTimeUnit;
    }

    private static List<U2FDeviceRegistration> getU2fDeviceRegistrations(final String username, final Collection<DeviceRegistration> devices) {
        return devices
            .stream()
            .map(d -> {
                val current = new U2FDeviceRegistration();
                current.setUsername(username);
                current.setRecord(d.toJson());
                current.setCreatedDate(LocalDate.now());
                return current;
            })
            .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends DeviceRegistration> getRegisteredDevices(final String username) {
        try {
            val devices = readDevicesFromResource();

            if (!devices.isEmpty()) {
                val devs = devices.get(MAP_KEY_DEVICES);
                val expirationDate = LocalDate.now().minus(this.expirationTime, DateTimeUtils.toChronoUnit(this.expirationTimeUnit));
                LOGGER.debug("Filtering devices for [{}] based on device expiration date [{}]", username, expirationDate);
                val list = devs
                    .stream()
                    .filter(d -> d.getUsername().equals(username) && (d.getCreatedDate().isAfter(expirationDate)))
                    .collect(Collectors.toList());

                LOGGER.debug("There are [{}] device(s) remaining in repository for [{}]", list.size(), username);
                return list.stream()
                    .map(r -> {
                        try {
                            return DeviceRegistration.fromJson(r.getRecord());
                        } catch (final Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ArrayList<>(0);
    }

    @Override
    @SneakyThrows
    public void authenticateDevice(final String username, final DeviceRegistration registration) {
        val devices = getRegisteredDevices(username);
        val matched = devices.stream().anyMatch(d -> d.equals(registration));
        if (!matched) {
            throw new AuthenticationFailedException("Failed to authenticate U2F device because "
                + "no matching record was found. Is device registered?");
        }
    }

    @Override
    public void registerDevice(final String username, final DeviceRegistration registration) {
        try {
            val device = new U2FDeviceRegistration();
            device.setUsername(username);
            device.setRecord(registration.toJson());
            device.setCreatedDate(LocalDate.now());

            val devices = readDevicesFromResource();
            val list = new ArrayList<U2FDeviceRegistration>(0);

            if (!devices.isEmpty()) {
                val devs = devices.get(MAP_KEY_DEVICES);
                LOGGER.debug("Located [{}] devices in repository", devs.size());
                list.addAll(new ArrayList<>(devs));
            }
            list.add(device);
            LOGGER.debug("There are [{}] device(s) remaining in repository. Storing...", list.size());
            writeDevicesBackToResource(list);

        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean isDeviceRegisteredFor(final String username) {
        return !getRegisteredDevices(username).isEmpty();
    }

    @Override
    public void clean() {
        try {
            val devices = readDevicesFromResource();
            if (!devices.isEmpty()) {
                val devs = devices.get(MAP_KEY_DEVICES);
                LOGGER.debug("Located [{}] devices in repository", devs.size());

                val expirationDate = LocalDate.now().minus(this.expirationTime, DateTimeUtils.toChronoUnit(this.expirationTimeUnit));
                LOGGER.debug("Filtering devices based on device expiration date [{}]", expirationDate);
                val list = devs.stream()
                    .filter(d -> d.getCreatedDate().isAfter(expirationDate))
                    .collect(Collectors.toList());

                LOGGER.debug("There are [{}] device(s) remaining in repository. Storing...", list.size());
                writeDevicesBackToResource(list);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
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
