package org.apereo.cas.adaptors.yubikey.registry;

import module java.base;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.util.CollectionUtils;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.val;

/**
 * This is {@link PermissiveYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Setter(AccessLevel.PROTECTED)
public class PermissiveYubiKeyAccountRegistry extends BaseYubiKeyAccountRegistry {
    protected Map<String, YubiKeyAccount> devices;

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

    /**
     * Remove a device from an account. The stored account is replaced by a copy without the device rather than changed
     * in place: the stored accounts may be read or serialized by other threads at the same time (the JSON registry
     * writes the whole map to its file after every change), and removing from the account's device list while that
     * happens fails the write with a {@link ConcurrentModificationException}.
     *
     * @param username the username
     * @param deviceId the device id
     */
    @Override
    public void delete(final String username, final long deviceId) {
        devices.computeIfPresent(username, (_, account) -> {
            val updated = account.clone();
            updated.getDevices().removeIf(device -> device.getId() == deviceId);
            return updated;
        });
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
