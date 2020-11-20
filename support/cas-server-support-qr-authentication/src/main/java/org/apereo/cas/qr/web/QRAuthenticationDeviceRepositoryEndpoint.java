package org.apereo.cas.qr.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.qr.authentication.QRAuthenticationDeviceRepository;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

import java.util.Collection;

/**
 * This is {@link QRAuthenticationDeviceRepositoryEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Endpoint(id = "qrDevices", enableByDefault = false)
public class QRAuthenticationDeviceRepositoryEndpoint extends BaseCasActuatorEndpoint {
    private final QRAuthenticationDeviceRepository repository;

    public QRAuthenticationDeviceRepositoryEndpoint(final CasConfigurationProperties casProperties,
        final QRAuthenticationDeviceRepository repository) {
        super(casProperties);
        this.repository = repository;
    }

    /**
     * Devices collection.
     *
     * @param principal the principal
     * @return the collection
     */
    @ReadOperation
    public Collection<String> devices(@Selector final String principal) {
        return repository.getAuthorizedDevicesFor(principal);
    }

    /**
     * Remove device.
     *
     * @param deviceId the device id
     */
    @DeleteOperation
    public void removeDevice(@Selector final String deviceId) {
        repository.removeDevice(deviceId);
    }

    /**
     * Register device.
     *
     * @param principal the principal
     * @param deviceId  the device id
     */
    @WriteOperation
    public void registerDevice(@Selector final String principal, @Selector final String deviceId) {
        repository.authorizeDeviceFor(principal, deviceId);
    }
}
