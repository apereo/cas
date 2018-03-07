package org.apereo.cas.adaptors.yubikey.registry;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;

import java.io.Serializable;

/**
 * This is {@link BaseYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 *
 * @since 5.2.0
 */
@Slf4j
@ToString
@NoArgsConstructor(force = true)
@Getter
@Setter
public abstract class BaseYubiKeyAccountRegistry implements YubiKeyAccountRegistry {

    /**
     * CipherExecutor.
     */
    private CipherExecutor<Serializable, String> cipherExecutor;

    /**
     * Account validator.
     */
    protected final YubiKeyAccountValidator accountValidator;

    public BaseYubiKeyAccountRegistry(final YubiKeyAccountValidator accountValidator) {
        this.accountValidator = accountValidator;
    }
}
