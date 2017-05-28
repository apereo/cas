package org.apereo.cas.adaptors.yubikey.registry;

import java.util.Map;

/**
 * This is {@link WhitelistYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class WhitelistYubiKeyAccountRegistry extends BaseYubiKeyAccountRegistry {
    private final Map<String, String> devices;

    public WhitelistYubiKeyAccountRegistry(final Map<String, String> devices) {
        this.devices = devices;
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid, final String yubikeyPublicId) {
        return devices.containsKey(uid) && devices.get(uid).equals(yubikeyPublicId);
    }
}
