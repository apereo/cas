package org.apereo.cas.adaptors.yubikey.registry;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;

import javax.persistence.NoResultException;
import java.io.Serializable;
import java.util.Optional;

/**
 * This is {@link BaseYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.2.0
 */
@Slf4j
@ToString
@RequiredArgsConstructor
@Getter
@Setter
public abstract class BaseYubiKeyAccountRegistry implements YubiKeyAccountRegistry {

    private final YubiKeyAccountValidator accountValidator;
    private CipherExecutor<Serializable, String> cipherExecutor = CipherExecutor.noOpOfSerializableToString();

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid) {
        try {
            return getAccount(uid).isPresent();
        } catch (final NoResultException e) {
            LOGGER.debug("No registration record could be found for id [{}]", uid);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid, final String yubikeyPublicId) {
        try {
            final Optional<YubiKeyAccount> account = getAccount(uid);
            if (account.isPresent()) {
                return account.get().getPublicId().equals(yubikeyPublicId);
            }
            return false;
        } catch (final NoResultException e) {
            LOGGER.debug("No registration record could be found for id [{}] and public id [{}]", uid, yubikeyPublicId);
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return false;
    }
}
