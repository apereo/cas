package org.apereo.cas.adaptors.yubikey.registry;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link OpenYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class OpenYubiKeyAccountRegistry extends BaseYubiKeyAccountRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenYubiKeyAccountRegistry.class);

    public OpenYubiKeyAccountRegistry() {
        LOGGER.warn("All credentials are considered eligible for YubiKey authentication. "
                        + "Consider providing an account registry implementation via [{}]",
                YubiKeyAccountRegistry.class.getName());
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid, final String yubikeyPublicId) {
        return true;
    }
}
