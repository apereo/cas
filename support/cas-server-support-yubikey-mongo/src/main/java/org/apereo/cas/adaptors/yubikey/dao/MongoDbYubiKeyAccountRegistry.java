package org.apereo.cas.adaptors.yubikey.dao;

import com.yubico.client.v2.YubicoClient;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.registry.BaseYubiKeyAccountRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link MongoDbYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class MongoDbYubiKeyAccountRegistry extends BaseYubiKeyAccountRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbYubiKeyAccountRegistry.class);
    
    public MongoDbYubiKeyAccountRegistry(final YubiKeyAccountValidator accountValidator) {
        super(accountValidator);
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid) {
        return true;
    }

    @Override
    public boolean registerAccountFor(final String uid, final String token) {
        if (accountValidator.isValid(uid, token)) {
            final String yubikeyPublicId = YubicoClient.getPublicId(token);
        }
        return false;
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid, final String yubikeyPublicId) {
        return false;
    }
}
