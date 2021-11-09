package org.apereo.cas.adaptors.yubikey.registry;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

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
            return account.getDevices()
                .stream()
                .map(device -> decodeYubikeyRegisteredDevice(device.getPublicId()))
                .filter(Objects::nonNull)
                .anyMatch(publicId -> publicId.equals(yubikeyPublicId));
        }
        return false;
    }

    @Override
    public Collection<? extends YubiKeyAccount> getAccountsInternal() {
        return new ArrayList<>(this.devices.values());
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
    public YubiKeyAccount save(final YubiKeyDeviceRegistrationRequest request,
                               final YubiKeyRegisteredDevice... device) {
        val yubiAccount = YubiKeyAccount.builder()
            .username(request.getUsername())
            .id(System.currentTimeMillis())
            .devices(CollectionUtils.wrapList(device))
            .build();
        return save(yubiAccount);
    }

    @Override
    public YubiKeyAccount save(final YubiKeyAccount yubiAccount) {
        devices.put(yubiAccount.getUsername(), yubiAccount);
        return yubiAccount;
    }

    @Override
    public boolean update(final YubiKeyAccount account) {
        devices.put(account.getUsername(), account);
        return true;
    }

    @Override
    protected YubiKeyAccount getAccountInternal(final String username) {
        if (devices.containsKey(username)) {
            val account = devices.get(username);
            return account.clone();
        }
        return null;
    }
}
