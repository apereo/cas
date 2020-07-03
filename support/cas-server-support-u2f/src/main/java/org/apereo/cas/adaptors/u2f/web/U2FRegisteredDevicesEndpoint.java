package org.apereo.cas.adaptors.u2f.web;

import org.apereo.cas.adaptors.u2f.storage.U2FDeviceRepository;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import com.yubico.u2f.data.DeviceRegistration;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.http.MediaType;

import java.util.Collection;

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

    @ReadOperation(produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<? extends DeviceRegistration> fetchAll() {
        return u2fDeviceRepository.getRegisteredDevices();
    }

    @ReadOperation(produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<? extends DeviceRegistration> fetchBy(@Selector final String username) {
        return u2fDeviceRepository.getRegisteredDevices(username);
    }
}
