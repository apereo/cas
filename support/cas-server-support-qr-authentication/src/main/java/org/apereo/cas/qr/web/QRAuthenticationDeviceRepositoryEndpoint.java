package org.apereo.cas.qr.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.qr.authentication.QRAuthenticationDeviceRepository;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
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
@Endpoint(id = "qrDevices", defaultAccess = Access.NONE)
public class QRAuthenticationDeviceRepositoryEndpoint extends BaseCasActuatorEndpoint {
    private final ObjectProvider<QRAuthenticationDeviceRepository> repository;

    public QRAuthenticationDeviceRepositoryEndpoint(final CasConfigurationProperties casProperties,
                                                    final ObjectProvider<QRAuthenticationDeviceRepository> repository) {
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
    @Operation(summary = "Get registered and authorized devices for the principal", parameters = @Parameter(name = "principal", required = true, description = "The principal to look up"))
    public Collection<String> devices(@Selector final String principal) {
        return repository.getObject().getAuthorizedDevicesFor(principal);
    }

    /**
     * Remove device.
     *
     * @param deviceId the device id
     */
    @DeleteOperation
    @Operation(summary = "Remove authorized device using the device id", parameters = @Parameter(name = "deviceId", required = true, description = "The device id to remove"))
    public void removeDevice(@Selector final String deviceId) {
        repository.getObject().removeDevice(deviceId);
    }

    /**
     * Register device.
     *
     * @param principal the principal
     * @param deviceId  the device id
     */
    @WriteOperation
    @Operation(summary = "Register device using the principal id and device id",
        parameters = {
            @Parameter(name = "principal", required = true, description = "The principal to register the device for"),
            @Parameter(name = "deviceId", required = true, description = "The device id to register")
        })
    public void registerDevice(@Selector final String principal, @Selector final String deviceId) {
        repository.getObject().authorizeDeviceFor(principal, deviceId);
    }
}
