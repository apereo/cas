package org.apereo.cas.adaptors.yubikey.registry;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;

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
    public boolean isYubiKeyRegisteredFor(final String uid, final String yubikeyPublicId) {
        return false;
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid) {
        return false;
    }

    @Override
    public boolean registerAccountFor(final String uid, final String yubikeyPublicId) {
        return false;
    }

    @Override
    public Optional<? extends YubiKeyAccount> getAccount(final String uid) {
        return Optional.empty();
    }

    @Override
    public Collection<? extends YubiKeyAccount> getAccounts() {
        return new ArrayList<>(0);
    }
}
