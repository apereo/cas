package org.apereo.cas.adaptors.yubikey.registry;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.persistence.NoResultException;

import java.io.Serializable;
import java.util.NoSuchElementException;

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
    public boolean isYubiKeyRegisteredFor(final String uid, final String yubikeyPublicId) {
        try {
            val account = getAccount(uid);
            if (account.isPresent()) {
                val yubiKeyAccount = account.get();
                return yubiKeyAccount.getDeviceIdentifiers()
                    .stream()
                    .anyMatch(pubId -> pubId.equals(yubikeyPublicId));
            }
        } catch (final NoSuchElementException | NoResultException e) {
            LOGGER.debug("No registration record could be found for id [{}] and public id [{}]", uid, yubikeyPublicId);
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return false;
    }

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
}
