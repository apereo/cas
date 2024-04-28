package org.apereo.cas.otp.repository.credentials;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.authentication.device.MultifactorAuthenticationDeviceManager;
import org.apereo.cas.authentication.device.MultifactorAuthenticationRegisteredDevice;
import org.apereo.cas.authentication.principal.Principal;
import lombok.RequiredArgsConstructor;
import java.util.List;
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

    @Override
    public List<MultifactorAuthenticationRegisteredDevice> findRegisteredDevices(final Principal principal) {
        return repository.get(principal.getId())
            .stream()
            .filter(Objects::nonNull)
            .map(this::mapAccount)
            .collect(Collectors.toList());
    }

    protected MultifactorAuthenticationRegisteredDevice mapAccount(final OneTimeTokenAccount acct) {
        return MultifactorAuthenticationRegisteredDevice
            .builder()
            .id(String.valueOf(acct.getId()))
            .name(acct.getName())
            .lastUsedDateTime(acct.getLastUsedDateTime())
            .source(acct.getSource())
            .payload(acct.toJson())
            .build();
    }

}
