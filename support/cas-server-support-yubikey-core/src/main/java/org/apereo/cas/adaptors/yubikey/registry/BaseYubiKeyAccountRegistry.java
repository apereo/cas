package org.apereo.cas.adaptors.yubikey.registry;

import com.yubico.client.v2.YubicoClient;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;

/**
 * This is {@link BaseYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public abstract class BaseYubiKeyAccountRegistry implements YubiKeyAccountRegistry {

    private final YubiKeyAccountValidator accountValidator;

    public BaseYubiKeyAccountRegistry(final YubiKeyAccountValidator accountValidator) {
        this.accountValidator = accountValidator;
    }

    public BaseYubiKeyAccountRegistry() {
        this(null);
    }

    @Override
    public boolean registerAccountFor(final String uid, final String token) {
        if (accountValidator.isValid(uid, token)) {
            final String yubikeyPublicId = YubicoClient.getPublicId(token);
            return registerAccount(uid, yubikeyPublicId);
        }
        return false;
    }

    /**
     * Register account.
     *
     * @param uid             the uid
     * @param yubikeyPublicId the yubikey public id
     * @return the boolean
     */
    protected abstract boolean registerAccount(String uid, String yubikeyPublicId);
}
