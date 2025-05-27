package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.device.MultifactorAuthenticationDeviceManager;
import org.apereo.cas.authentication.device.MultifactorAuthenticationRegisteredDevice;
import org.apereo.cas.authentication.principal.Principal;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link YubiKeyMultifactorAuthenticatorDeviceManager}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
public class YubiKeyMultifactorAuthenticatorDeviceManager implements MultifactorAuthenticationDeviceManager {
    private final YubiKeyAccountRegistry yubiKeyAccountRegistry;
    private final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider;

    @Override
    public List<String> getSource() {
        return List.of("YubiKey");
    }

    @Override
    public List<MultifactorAuthenticationRegisteredDevice> findRegisteredDevices(final Principal principal) {
        val registrations = yubiKeyAccountRegistry.getAccount(principal.getId());
        return registrations
            .stream()
            .map(this::mapAccount)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    @Override
    public void removeRegisteredDevice(final Principal principal, final String deviceId) {
        yubiKeyAccountRegistry.delete(principal.getId(), Long.parseLong(deviceId));
    }

    protected List<MultifactorAuthenticationRegisteredDevice> mapAccount(final YubiKeyAccount acct) {
        return acct
            .getDevices()
            .stream()
            .map(device -> MultifactorAuthenticationRegisteredDevice.builder()
                .id(String.valueOf(device.getId()))
                .name(device.getName())
                .payload(device.toJson())
                .lastUsedDateTime(device.getRegistrationDate().toString())
                .model(device.getPublicId())
                .source(getSource().getFirst())
                .details(Map.of("providerId", multifactorAuthenticationProvider.getObject().getId()))
                .build())
            .collect(Collectors.toList());
    }
}
