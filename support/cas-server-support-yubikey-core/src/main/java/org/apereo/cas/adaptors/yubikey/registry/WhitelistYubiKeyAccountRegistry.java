package org.apereo.cas.adaptors.yubikey.registry;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link WhitelistYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class WhitelistYubiKeyAccountRegistry extends BaseYubiKeyAccountRegistry {
    /**
     * Device registrations.
     */
    protected final Map<String, String> devices;

    public WhitelistYubiKeyAccountRegistry(final Map<String, String> devices,
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
            final var pubId = devices.get(uid);
            return getCipherExecutor().decode(pubId).equals(yubikeyPublicId);
        }
        return false;
    }

    @Override
    public boolean registerAccountFor(final String uid, final String token) {
        if (getAccountValidator().isValid(uid, token)) {
            final var yubikeyPublicId = getAccountValidator().getTokenPublicId(token);
            final var pubId = getCipherExecutor().encode(yubikeyPublicId);
            devices.put(uid, pubId);
            return isYubiKeyRegisteredFor(uid, yubikeyPublicId);
        }
        return false;
    }

    @Override
    public Collection<YubiKeyAccount> getAccounts() {
        return this.devices.entrySet().stream()
            .map(entry -> new YubiKeyAccount(System.currentTimeMillis(),
                entry.getKey(),
                getCipherExecutor().decode(entry.getValue())))
            .collect(Collectors.toSet());
    }

    @Override
    public Optional<YubiKeyAccount> getAccount(final String uid) {
        if (devices.containsKey(uid)) {
            final var publicId = getCipherExecutor().decode(devices.get(uid));
            return Optional.of(new YubiKeyAccount(System.currentTimeMillis(), publicId, uid));
        }
        return Optional.empty();
    }
}
