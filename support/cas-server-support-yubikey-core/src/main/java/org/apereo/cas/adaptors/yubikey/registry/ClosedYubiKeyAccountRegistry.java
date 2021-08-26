package org.apereo.cas.adaptors.yubikey.registry;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * This is {@link ClosedYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ClosedYubiKeyAccountRegistry extends BaseYubiKeyAccountRegistry {
    public ClosedYubiKeyAccountRegistry(final YubiKeyAccountValidator accountValidator) {
        super(accountValidator);
    }

    @Override
    protected YubiKeyAccount getAccountInternal(final String username) {
        return null;
    }

    @Override
    public void delete(final String username, final long deviceId) {
    }

    @Override
    public void delete(final String uid) {
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid, final String yubikeyPublicId) {
        return false;
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid) {
        return false;
    }

    @Override
    public boolean registerAccountFor(final YubiKeyDeviceRegistrationRequest request) {
        return false;
    }

    @Override
    public YubiKeyAccount save(final YubiKeyDeviceRegistrationRequest request,
                                  final YubiKeyRegisteredDevice... device) {
        return null;
    }

    @Override
    public YubiKeyAccount save(final YubiKeyAccount account) {
        return null;
    }

    @Override
    public boolean update(final YubiKeyAccount account) {
        return false;
    }

    @Override
    public Optional<? extends YubiKeyAccount> getAccount(final String uid) {
        return Optional.empty();
    }

    @Override
    public Collection<? extends YubiKeyAccount> getAccountsInternal() {
        return new ArrayList<>(0);
    }


    @Override
    public void deleteAll() {
    }
}
