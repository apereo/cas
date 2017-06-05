package org.apereo.cas.adaptors.yubikey.registry;

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
}
