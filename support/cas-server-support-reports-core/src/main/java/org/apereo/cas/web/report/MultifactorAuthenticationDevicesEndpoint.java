package org.apereo.cas.web.report;

import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.device.MultifactorAuthenticationRegisteredDevice;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.val;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.Objects;

/**
 * This is {@link MultifactorAuthenticationDevicesEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Endpoint(id = "mfaDevices", defaultAccess = Access.NONE)
public class MultifactorAuthenticationDevicesEndpoint extends BaseCasRestActuatorEndpoint {

    public MultifactorAuthenticationDevicesEndpoint(final CasConfigurationProperties casProperties,
                                                    final ConfigurableApplicationContext applicationContext) {
        super(casProperties, applicationContext);
    }

    /**
     * All mfa devices for user.
     *
     * @param username the username
     * @return the list
     * @throws Throwable the throwable
     */
    @GetMapping(value = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all present and registered MFA devices for a given user",
        parameters = @Parameter(name = "username", description = "The user that owns registered multifactor devices", in = ParameterIn.PATH))
    public List<MultifactorAuthenticationRegisteredDevice> allMfaDevicesForUser(@PathVariable final String username) throws Throwable {
        val principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(username);
        val providers = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext).values();
        return providers
            .stream()
            .filter(provider -> Objects.nonNull(provider.getDeviceManager()))
            .map(provider -> provider.getDeviceManager().findRegisteredDevices(principal))
            .flatMap(List::stream)
            .toList();

    }

    /**
     * Remove mfa device for user.
     *
     * @param username   the username
     * @param key        the key
     * @param providerId the provider id
     * @throws Throwable the throwable
     */
    @DeleteMapping(value = "/{username}/{providerId}/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete registered MFA device for a given user",
        parameters = {
            @Parameter(name = "username", description = "The user that owns registered multifactor devices", in = ParameterIn.PATH),
            @Parameter(name = "providerId", description = "The multifactor provider ID that owns the device", in = ParameterIn.PATH),
            @Parameter(name = "key", description = "The device id or key that belongs to the user and needs to be removed", in = ParameterIn.PATH)
        })
    public void removeMfaDeviceForUser(@PathVariable final String username,
                                       @PathVariable final String key,
                                       @PathVariable final String providerId) throws Throwable {
        val principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(username);
        val providers = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext).values();
        providers
            .stream()
            .filter(provider -> provider.matches(providerId))
            .filter(provider -> Objects.nonNull(provider.getDeviceManager()))
            .forEach(provider -> provider.getDeviceManager().removeRegisteredDevice(principal, key));
    }
}
