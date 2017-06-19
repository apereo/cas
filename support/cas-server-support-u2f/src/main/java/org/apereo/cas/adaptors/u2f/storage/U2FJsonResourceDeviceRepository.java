package org.apereo.cas.adaptors.u2f.storage;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.cache.LoadingCache;
import com.yubico.u2f.data.DeviceRegistration;
import org.apereo.cas.util.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import javax.mail.AuthenticationFailedException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This is {@link U2FJsonResourceDeviceRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class U2FJsonResourceDeviceRepository extends BaseU2FDeviceRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(U2FJsonResourceDeviceRepository.class);
    private static final String MAP_KEY_SERVICES = "services";

    private final ObjectMapper mapper;

    private final long expirationTime;
    private final TimeUnit expirationTimeUnit;
    private final Resource jsonResource;

    public U2FJsonResourceDeviceRepository(final LoadingCache<String, String> requestStorage,
                                           final Resource jsonResource,
                                           final long expirationTime, final TimeUnit expirationTimeUnit) {
        super(requestStorage);
        this.expirationTime = expirationTime;
        this.expirationTimeUnit = expirationTimeUnit;
        this.jsonResource = jsonResource;

        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        try {
            if (this.jsonResource.exists()) {
                if (this.jsonResource.getFile().createNewFile()) {
                    LOGGER.debug("Created JSON resource [{}] for U2F device registrations", jsonResource);
                }
            }
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public Collection<DeviceRegistration> getRegisteredDevices(final String username) {
        try {

            if (!this.jsonResource.getFile().exists() || this.jsonResource.getFile().length() <= 0) {
                LOGGER.debug("JSON resource [{}] does not exist or is empty", jsonResource);
                return new ArrayList<>();
            }


            final Map<String, List<U2FDeviceRegistration>> devices = readDevicesFromJsonResource();

            if (!devices.isEmpty()) {
                final List<U2FDeviceRegistration> devs = devices.get(MAP_KEY_SERVICES);
                final LocalDate expirationDate = LocalDate.now().minus(this.expirationTime, DateTimeUtils.toChronoUnit(this.expirationTimeUnit));
                final List<U2FDeviceRegistration> list = devs
                        .stream()
                        .filter(d -> d.getUsername().equals(username)
                                && (d.getDate().isEqual(expirationDate) || d.getDate().isAfter(expirationDate)))
                        .collect(Collectors.toList());

                return list.stream()
                        .map(d -> DeviceRegistration.fromJson(d.getRecord()))
                        .collect(Collectors.toList());
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    private Map<String, List<U2FDeviceRegistration>> readDevicesFromJsonResource() throws java.io.IOException {
        return mapper.readValue(jsonResource.getInputStream(),
                new TypeReference<Map<String, List<U2FDeviceRegistration>>>() {
                });
    }


    @Override
    public void authenticateDevice(final String username, final DeviceRegistration registration) {
        try {
            final Collection<DeviceRegistration> devices = getRegisteredDevices(username);
            final boolean matched = devices.stream().anyMatch(d -> d.equals(registration));
            if (!matched) {
                throw new AuthenticationFailedException("Failed to authenticate U2F device because "
                        + "no matching record was found. Is device registered?");
            }
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private static List<U2FDeviceRegistration> getU2fDeviceRegistrations(final String username, final Collection<DeviceRegistration> devices) {
        return devices
                .stream()
                .map(d -> {
                    final U2FDeviceRegistration current = new U2FDeviceRegistration();
                    current.setUsername(username);
                    current.setRecord(d.toJson());
                    current.setDate(LocalDate.now());
                    return current;
                })
                .collect(Collectors.toList());
    }

    private void writeDevicesBackToJsonResource(final List<U2FDeviceRegistration> list) throws Exception {
        final Map<String, List<U2FDeviceRegistration>> newDevices = new HashMap<>();
        newDevices.put(MAP_KEY_SERVICES, list);
        mapper.writerWithDefaultPrettyPrinter().writeValue(jsonResource.getFile(), newDevices);
    }

    @Override
    public void registerDevice(final String username, final DeviceRegistration registration) {
        try {
            final U2FDeviceRegistration device = new U2FDeviceRegistration();
            device.setUsername(username);
            device.setRecord(registration.toJson());
            device.setDate(LocalDate.now());

            final Collection<DeviceRegistration> devices = getRegisteredDevices(username);
            final List<U2FDeviceRegistration> list = getU2fDeviceRegistrations(username, devices);
            list.add(device);
            writeDevicesBackToJsonResource(list);
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
            final Map<String, List<U2FDeviceRegistration>> devices = readDevicesFromJsonResource();
            if (!devices.isEmpty()) {
                final List<U2FDeviceRegistration> devs = devices.get(MAP_KEY_SERVICES);
                LOGGER.debug("Located [{}] devices in repository", devs.size());

                final LocalDate expirationDate = LocalDate.now().minus(this.expirationTime, DateTimeUtils.toChronoUnit(this.expirationTimeUnit));
                LOGGER.debug("Filtering devices based on device expiration date [{}]", expirationDate);
                final List<U2FDeviceRegistration> list = devs.stream()
                        .filter(d -> d.getDate().isEqual(expirationDate) || d.getDate().isBefore(expirationDate))
                        .collect(Collectors.toList());

                LOGGER.debug("There are [{}] device(s) remaining in repository. Storing...", list.size());
                writeDevicesBackToJsonResource(list);
                LOGGER.debug("Saved [{}] device(s) into repository [{}]", list.size(), jsonResource);
            }

        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
