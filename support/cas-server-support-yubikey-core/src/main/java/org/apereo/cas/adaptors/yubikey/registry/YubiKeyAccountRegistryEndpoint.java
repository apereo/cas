package org.apereo.cas.adaptors.yubikey.registry;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

import java.util.Collection;

/**
 * This is {@link YubiKeyAccountRegistryEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
@Endpoint(id = "yubikey-account-repository", enableByDefault = false)
public class YubiKeyAccountRegistryEndpoint {
    /**
     * The Registry.
     */
    private final YubiKeyAccountRegistry registry;

    /**
     * Get yubi key account.
     *
     * @param username the username
     * @return the yubi key account
     */
    @ReadOperation
    public YubiKeyAccount get(@Selector final String username) {
        val result = registry.getAccount(username);
        if (result.isPresent()) {
            return result.get();
        }
        return null;
    }

    /**
     * Load account collection.
     *
     * @return the collection
     */
    @ReadOperation
    public Collection<? extends YubiKeyAccount> load() {
        return registry.getAccounts();
    }

    /**
     * Delete.
     *
     * @param username the username
     */
    @DeleteOperation
    public void delete(@Selector final String username) {
        registry.delete(username);
    }

    /**
     * Delete all.
     */
    @DeleteOperation
    public void deleteAll() {
        registry.deleteAll();
    }
}
