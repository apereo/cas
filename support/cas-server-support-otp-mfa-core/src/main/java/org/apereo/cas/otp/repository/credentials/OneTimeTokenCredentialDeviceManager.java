package org.apereo.cas.otp.repository.credentials;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.authentication.device.MultifactorAuthenticationDeviceManager;
import org.apereo.cas.authentication.device.MultifactorAuthenticationRegisteredDevice;
import org.apereo.cas.authentication.principal.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link OneTimeTokenCredentialDeviceManager}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
public class OneTimeTokenCredentialDeviceManager implements MultifactorAuthenticationDeviceManager {
    private final OneTimeTokenCredentialRepository repository;
    private final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider;

    @Override
    public List<String> getSource() {
        return List.of("Google Authenticator");
    }

    @Override
    public List<MultifactorAuthenticationRegisteredDevice> findRegisteredDevices(final Principal principal) {
        return repository.get(principal.getId())
            .stream()
            .filter(Objects::nonNull)
            .map(this::mapAccount)
            .collect(Collectors.toList());
    }

    @Override
    public void removeRegisteredDevice(final Principal principal, final String deviceId) {
        repository.delete(Long.parseLong(deviceId));
    }

    protected MultifactorAuthenticationRegisteredDevice mapAccount(final OneTimeTokenAccount acct) {
        return MultifactorAuthenticationRegisteredDevice
            .builder()
            .id(String.valueOf(acct.getId()))
            .name(acct.getName())
            .lastUsedDateTime(acct.getLastUsedDateTime())
            .source(acct.getSource())
            .payload(acct.toJson())
            .details(Map.of("providerId", multifactorAuthenticationProvider.getObject().getId()))
            .build();
    }
}
