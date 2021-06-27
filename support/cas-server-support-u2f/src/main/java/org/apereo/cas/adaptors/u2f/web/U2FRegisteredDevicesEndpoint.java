package org.apereo.cas.adaptors.u2f.web;

import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRegistration;
import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.val;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * This is {@link U2FRegisteredDevicesEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Endpoint(id = "u2fDevices", enableByDefault = false)
public class U2FRegisteredDevicesEndpoint extends BaseCasActuatorEndpoint {
    private final U2FDeviceRepository u2fDeviceRepository;

    public U2FRegisteredDevicesEndpoint(final CasConfigurationProperties casProperties,
                                        final U2FDeviceRepository u2fDeviceRepository) {
        super(casProperties);
        this.u2fDeviceRepository = u2fDeviceRepository;
    }

    /**
     * Fetch all and provide collection.
     *
     * @return the collection
     */
    @ReadOperation(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all registered devices")
    public Collection<? extends U2FDeviceRegistration> fetchAll() {
        return u2fDeviceRepository.getRegisteredDevices()
            .stream()
            .map(u2fDeviceRepository::decode)
            .collect(Collectors.toList());
    }

    /**
     * Fetch by username and provide collection.
     *
     * @param username the username
     * @return the collection
     */
    @ReadOperation(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all registered devices for the user", parameters = {@Parameter(name = "username", required = true)})
    public Collection<? extends U2FDeviceRegistration> fetchBy(@Selector final String username) {
        return u2fDeviceRepository.getRegisteredDevices(username)
            .stream()
            .map(u2fDeviceRepository::decode)
            .collect(Collectors.toList());
    }

    /**
     * Delete.
     *
     * @param username the username
     */
    @DeleteOperation
    @Operation(summary = "Delete all registered devices", parameters = {@Parameter(name = "username", required = true)})
    public void delete(@Selector final String username) {
        val registeredDevices = new ArrayList<>(u2fDeviceRepository.getRegisteredDevices(username));
        registeredDevices.forEach(u2fDeviceRepository::deleteRegisteredDevice);
    }

    /**
     * Delete.
     *
     * @param username the username
     * @param id       the id
     */
    @DeleteOperation
    @Operation(summary = "Delete registered device for username and device",
        parameters = {@Parameter(name = "username", required = true), @Parameter(name = "id", required = true)})
    public void delete(@Selector final String username, @Selector final Long id) {
        val registeredDevices = new ArrayList<>(u2fDeviceRepository.getRegisteredDevices(username));
        registeredDevices
            .stream()
            .filter(d -> d.getId() == id)
            .forEach(u2fDeviceRepository::deleteRegisteredDevice);
    }
}
