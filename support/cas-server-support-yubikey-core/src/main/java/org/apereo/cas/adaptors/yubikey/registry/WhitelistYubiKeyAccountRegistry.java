package org.apereo.cas.adaptors.yubikey.registry;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;

import lombok.val;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link WhitelistYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class WhitelistYubiKeyAccountRegistry extends BaseYubiKeyAccountRegistry {
    /**
     * Device registrations.
     */
    protected final MultiValueMap<String, String> devices;

    public WhitelistYubiKeyAccountRegistry(final MultiValueMap<String, String> devices,
                                           final YubiKeyAccountValidator validator) {
        super(validator);
        this.devices = devices;
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid) {
        return devices.containsKey(uid);
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid, final String yubikeyPublicId) {
        if (devices.containsKey(uid)) {
            val pubIds = devices.get(uid);
            return pubIds.stream()
                .anyMatch(pubId -> getCipherExecutor().decode(pubId).equals(yubikeyPublicId));
        }
        return false;
    }

    @Override
    public boolean registerAccountFor(final String uid, final String token) {
        val accountValidator = getAccountValidator();
        if (accountValidator.isValid(uid, token)) {
            val yubikeyPublicId = accountValidator.getTokenPublicId(token);
            val pubId = getCipherExecutor().encode(yubikeyPublicId);
            devices.add(uid, pubId);
            return isYubiKeyRegisteredFor(uid, yubikeyPublicId);
        }
        return false;
    }

    @Override
    public Collection<? extends YubiKeyAccount> getAccounts() {
        return this.devices.entrySet().stream()
            .map(entry -> {
                val values = entry.getValue()
                    .stream()
                    .map(value -> getCipherExecutor().decode(value))
                    .collect(Collectors.toCollection(ArrayList::new));
                return new YubiKeyAccount(System.currentTimeMillis(), values, entry.getKey());
            })
            .collect(Collectors.toSet());
    }

    @Override
    public Optional<? extends YubiKeyAccount> getAccount(final String uid) {
        if (devices.containsKey(uid)) {
            val pubIds = devices.get(uid);

            val values = pubIds.stream()
                .map(value -> getCipherExecutor().decode(value))
                .collect(Collectors.toCollection(ArrayList::new));
            return Optional.of(new YubiKeyAccount(System.currentTimeMillis(), values, uid));
        }
        return Optional.empty();
    }
}
