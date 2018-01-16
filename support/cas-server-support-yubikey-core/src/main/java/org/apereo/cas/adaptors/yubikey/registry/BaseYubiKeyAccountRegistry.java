package org.apereo.cas.adaptors.yubikey.registry;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;

/**
 * This is {@link BaseYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public abstract class BaseYubiKeyAccountRegistry implements YubiKeyAccountRegistry {

    /** Account validator. */
    protected final YubiKeyAccountValidator accountValidator;

    public BaseYubiKeyAccountRegistry(final YubiKeyAccountValidator accountValidator) {
        this.accountValidator = accountValidator;
    }

    public BaseYubiKeyAccountRegistry() {
        this(null);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
