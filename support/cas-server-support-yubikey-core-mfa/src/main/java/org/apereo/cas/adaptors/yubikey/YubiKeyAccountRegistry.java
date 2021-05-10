package org.apereo.cas.adaptors.yubikey;

import org.apereo.cas.util.crypto.CipherExecutor;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;

/**
 * General contract that allows one to determine whether
 * a particular YubiKey account
 * is allowed to participate in the authentication.
 * Accounts are noted by the username
 * and the public id of the YubiKey device.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public interface YubiKeyAccountRegistry {
    /**
     * Determines whether the registered
     * YubiKey public id is allowed for the {@code username} received.
     *
     * @param username        user id
     * @param yubikeyPublicId public id of the yubi id
     * @return true if the public id is allowed and registered for the username.
     */
    boolean isYubiKeyRegisteredFor(String username, String yubikeyPublicId);

    /**
     * Is yubi key registered for boolean.
     *
     * @param username the username
     * @return true/false
     */
    boolean isYubiKeyRegisteredFor(String username);

    /**
     * Register account/device.
     *
     * @param request the request
     * @return true /false
     */
    boolean registerAccountFor(YubiKeyDeviceRegistrationRequest request);

    /**
     * Save.
     *
     * @param account the account
     * @return the yubi key account
     */
    YubiKeyAccount save(YubiKeyAccount account);

    /**
     * Gets accounts for all users.
     *
     * @return the accounts
     */
    Collection<? extends YubiKeyAccount> getAccounts();

    /**
     * Gets account.
     *
     * @param username the username
     * @return the account
     */
    Optional<? extends YubiKeyAccount> getAccount(String username);

    /**
     * Gets account validator.
     *
     * @return the account validator
     */
    YubiKeyAccountValidator getAccountValidator();

    /**
     * Gets cipher executor.
     *
     * @return the cipher executor
     */
    CipherExecutor<Serializable, String> getCipherExecutor();

    /**
     * Delete.
     *
     * @param username the username
     * @param deviceId the device id
     */
    void delete(String username, long deviceId);

    /**
     * Delete.
     *
     * @param username the username
     */
    void delete(String username);

    /**
     * Delete all.
     */
    void deleteAll();

    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return getClass().getSimpleName();
    }
}
