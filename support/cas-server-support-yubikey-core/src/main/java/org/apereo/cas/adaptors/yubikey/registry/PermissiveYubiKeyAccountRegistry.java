package org.apereo.cas.adaptors.yubikey.registry;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link PermissiveYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class PermissiveYubiKeyAccountRegistry extends BaseYubiKeyAccountRegistry {
    /**
     * Device registrations.
     */
    protected final Map<String, YubiKeyAccount> devices;

    public PermissiveYubiKeyAccountRegistry(final Map<String, YubiKeyAccount> devices,
                                            final YubiKeyAccountValidator validator) {
        super(validator);
        this.devices = devices;
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid) {
        val account = getAccount(uid);
        return account.isPresent() && !account.get().getDevices().isEmpty();
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid, final String yubikeyPublicId) {
        if (devices.containsKey(uid)) {
            val account = devices.get(uid);
            return account.getDevices().stream()
                .anyMatch(device -> getCipherExecutor().decode(device.getPublicId()).equals(yubikeyPublicId));
        }
        return false;
    }

    @Override
    public Collection<? extends YubiKeyAccount> getAccountsInternal() {
        return this.devices.values();
    }

    @Override
    public Optional<? extends YubiKeyAccount> getAccount(final String uid) {
        if (devices.containsKey(uid)) {
            val account = devices.get(uid);
            val yubiAccount = account.clone();

            yubiAccount.getDevices().forEach(device -> {
                val decoded = getCipherExecutor().decode(device.getPublicId());
                device.setPublicId(decoded);
            });
            return Optional.of(yubiAccount);
        }
        return Optional.empty();
    }

    @Override
    public void delete(final String username, final long deviceId) {
        if (devices.containsKey(username)) {
            devices.get(username).getDevices().removeIf(device -> device.getId() == deviceId);
        }
    }

    @Override
    public void delete(final String uid) {
        this.devices.remove(uid);
    }

    @Override
    public void deleteAll() {
        this.devices.clear();
    }

    @Override
    protected YubiKeyAccount saveAccount(final YubiKeyDeviceRegistrationRequest request,
                                         final YubiKeyRegisteredDevice... device) {
        val yubiAccount = YubiKeyAccount.builder()
            .username(request.getUsername())
            .id(System.currentTimeMillis())
            .devices(CollectionUtils.wrapList(device))
            .build();
        devices.put(request.getUsername(), yubiAccount);
        return yubiAccount;
    }

    @Override
    protected boolean update(final YubiKeyAccount account) {
        devices.put(account.getUsername(), account);
        return true;
    }
}
