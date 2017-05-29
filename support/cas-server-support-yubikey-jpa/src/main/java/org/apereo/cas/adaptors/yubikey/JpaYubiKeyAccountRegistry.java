package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.adaptors.yubikey.registry.BaseYubiKeyAccountRegistry;

/**
 * This is {@link JpaYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class JpaYubiKeyAccountRegistry extends BaseYubiKeyAccountRegistry {

    public JpaYubiKeyAccountRegistry(final YubiKeyAccountValidator accountValidator) {
        super(accountValidator);
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid) {
        return true;
    }

    @Override
    public boolean registerAccount(final String uid, final String yubikeyPublicId) {
        return false;
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid, final String yubikeyPublicId) {
        return false;
    }
}
