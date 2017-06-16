package org.apereo.cas.adaptors.yubikey.registry;

import com.yubico.client.v2.YubicoClient;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;

import java.util.Map;

/**
 * This is {@link WhitelistYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class WhitelistYubiKeyAccountRegistry extends BaseYubiKeyAccountRegistry {
    /** Device registrations. */
    protected final Map<String, String> devices;

    public WhitelistYubiKeyAccountRegistry(final Map<String, String> devices,
                                           final YubiKeyAccountValidator validator) {
        super(validator);
        this.devices = devices;
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid) {
        return devices.containsKey(uid);
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid, final String yubikeyPublicId) {
        return devices.containsKey(uid) && devices.get(uid).equals(yubikeyPublicId);
    }

    @Override
    public boolean registerAccountFor(final String uid, final String token) {
        if (accountValidator.isValid(uid, token)) {
            final String yubikeyPublicId = YubicoClient.getPublicId(token);
            devices.put(uid, yubikeyPublicId);
            return isYubiKeyRegisteredFor(uid, yubikeyPublicId);
        }
        return false;
    }
}
