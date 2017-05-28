package org.apereo.cas.adaptors.yubikey.registry;

import org.springframework.core.io.Resource;

/**
 * This is {@link JsonYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class JsonYubiKeyAccountRegistry extends BaseYubiKeyAccountRegistry {
    private final Resource jsonFile;

    public JsonYubiKeyAccountRegistry(final Resource jsonFile) {
        this.jsonFile = jsonFile;
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid, final String yubikeyPublicId) {
        return false;
    }
}
