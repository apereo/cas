package org.apereo.cas.adaptors.yubikey.registry;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

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
@Endpoint(id = "yubikeyAccountRepository", enableByDefault = false)
public class YubiKeyAccountRegistryEndpoint extends BaseCasActuatorEndpoint {
    /**
     * The Registry.
     */
    private final YubiKeyAccountRegistry registry;

    public YubiKeyAccountRegistryEndpoint(final CasConfigurationProperties casProperties,
                                          final YubiKeyAccountRegistry registry) {
        super(casProperties);
        this.registry = registry;
    }

    /**
     * Get yubi key account.
     *
     * @param username the username
     * @return the yubi key account
     */
    @ReadOperation
    public YubiKeyAccount get(@Selector final String username) {
        val result = registry.getAccount(username);
        return result.orElse(null);
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
