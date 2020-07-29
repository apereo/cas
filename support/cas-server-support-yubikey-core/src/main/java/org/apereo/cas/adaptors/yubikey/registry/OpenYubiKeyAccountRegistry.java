package org.apereo.cas.adaptors.yubikey.registry;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.util.CollectionUtils;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * This is {@link OpenYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class OpenYubiKeyAccountRegistry extends BaseYubiKeyAccountRegistry {
    public OpenYubiKeyAccountRegistry(final YubiKeyAccountValidator accountValidator) {
        super(accountValidator);
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid, final String yubikeyPublicId) {
        return true;
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid) {
        return true;
    }

    @Override
    public boolean registerAccountFor(final YubiKeyDeviceRegistrationRequest request) {
        return true;
    }

    @Override
    public Optional<? extends YubiKeyAccount> getAccount(final String uid) {
        return Optional.of(YubiKeyAccount.builder()
            .username(uid)
            .id(System.currentTimeMillis())
            .devices(CollectionUtils.wrapArrayList(YubiKeyRegisteredDevice.builder()
                .id(System.currentTimeMillis())
                .name(UUID.randomUUID().toString())
                .publicId(UUID.randomUUID().toString())
                .registrationDate(ZonedDateTime.now(ZoneOffset.UTC))
                .build()))
            .build());
    }

    @Override
    public Collection<? extends YubiKeyAccount> getAccounts() {
        return new ArrayList<>(0);
    }
}
