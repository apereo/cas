package org.apereo.cas.adaptors.yubikey.registry;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link OpenYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class OpenYubiKeyAccountRegistry extends BaseYubiKeyAccountRegistry {
    @Override
    public boolean isYubiKeyRegisteredFor(final String uid, final String yubikeyPublicId) {
        return true;
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid) {
        return true;
    }

    @Override
    public boolean registerAccountFor(final String uid, final String yubikeyPublicId) {
        return true;
    }

    @Override
    public Collection<YubiKeyAccount> getAccounts() {
        return new ArrayList<>(0);
    }
}
