package org.apereo.cas.adaptors.yubikey.registry;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public boolean registerAccountFor(final YubiKeyDeviceRegistrationRequest request) {
        val accountValidator = getAccountValidator();
        if (accountValidator.isValid(request.getUsername(), request.getToken())) {
            val yubikeyPublicId = accountValidator.getTokenPublicId(request.getToken());
            val pubId = getCipherExecutor().encode(yubikeyPublicId);

            val device = YubiKeyRegisteredDevice.builder()
                .id(System.currentTimeMillis())
                .name(request.getName())
                .publicId(pubId)
                .registrationDate(ZonedDateTime.now(Clock.systemUTC()))
                .build();
            getAccount(request.getUsername()).ifPresentOrElse(account -> {
                account.getDevices().add(device);
                devices.put(request.getUsername(), account);
            },
                () -> {
                    val yubiAccount = YubiKeyAccount.builder()
                        .username(request.getUsername())
                        .id(System.currentTimeMillis())
                        .devices(CollectionUtils.wrapList(device))
                        .build();
                    devices.put(request.getUsername(), yubiAccount);
                });
            return isYubiKeyRegisteredFor(request.getUsername(), yubikeyPublicId);
        }
        return false;
    }

    @Override
    public Collection<? extends YubiKeyAccount> getAccounts() {
        return this.devices.values()
            .stream()
            .map(account -> getAccount(account.getUsername()).orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
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
}
