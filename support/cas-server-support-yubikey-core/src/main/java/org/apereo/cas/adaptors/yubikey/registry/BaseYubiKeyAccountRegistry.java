package org.apereo.cas.adaptors.yubikey.registry;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountRegistry;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link BaseYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.2.0
 */
@Slf4j
@ToString
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Transactional(transactionManager = "transactionManagerYubiKey")
public abstract class BaseYubiKeyAccountRegistry implements YubiKeyAccountRegistry {

    private final YubiKeyAccountValidator accountValidator;

    private CipherExecutor<Serializable, String> cipherExecutor = CipherExecutor.noOpOfSerializableToString();

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid, final String yubikeyPublicId) {
        try {
            val account = getAccount(uid);
            if (account.isPresent()) {
                val yubiKeyAccount = account.get();
                return yubiKeyAccount.getDevices()
                    .stream()
                    .anyMatch(device -> device.getPublicId().equals(yubikeyPublicId));
            }
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid) {
        try {
            val account = getAccount(uid);
            return account.isPresent() && !account.get().getDevices().isEmpty();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

    @Override
    public final Collection<? extends YubiKeyAccount> getAccounts() {
        val currentDevices = getAccountsInternal();
        return currentDevices
            .stream()
            .map(it -> buildAndDecodeYubiKeyAccount(it).orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public boolean registerAccountFor(final YubiKeyDeviceRegistrationRequest request) {
        if (accountValidator.isValid(request.getUsername(), request.getToken())) {
            val yubikeyPublicId = getCipherExecutor().encode(accountValidator.getTokenPublicId(request.getToken()));

            val device = YubiKeyRegisteredDevice.builder()
                .id(System.currentTimeMillis())
                .name(request.getName())
                .publicId(yubikeyPublicId)
                .registrationDate(ZonedDateTime.now(Clock.systemUTC()))
                .build();

            var account = getAccountInternal(request.getUsername());
            if (account == null) {
                return save(request, device) != null;
            }
            account.getDevices().add(device);
            return update(account);
        }
        return false;
    }

    @Override
    public Optional<? extends YubiKeyAccount> getAccount(final String username) {
        try {
            val account = getAccountInternal(username);
            if (account != null) {
                return buildAndDecodeYubiKeyAccount(account);
            }
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Save account.
     *
     * @param request the request
     * @param device  the device
     * @return the account
     */
    public abstract YubiKeyAccount save(YubiKeyDeviceRegistrationRequest request, YubiKeyRegisteredDevice... device);

    /**
     * Update.
     *
     * @param account the account
     * @return true/false
     */
    public abstract boolean update(YubiKeyAccount account);

    private Optional<? extends YubiKeyAccount> buildAndDecodeYubiKeyAccount(final YubiKeyAccount account) {
        val yubiKeyAccount = account.clone();
        val devices = yubiKeyAccount.getDevices()
            .stream()
            .map(device -> decodeYubiKeyRegisteredDevice(account, device))
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(ArrayList::new));
        yubiKeyAccount.setDevices(devices);
        return Optional.of(yubiKeyAccount);
    }

    private YubiKeyRegisteredDevice decodeYubiKeyRegisteredDevice(final YubiKeyAccount account,
                                                                  final YubiKeyRegisteredDevice device) {
        val pubId = decodeYubikeyRegisteredDevice(device.getPublicId());
        if (StringUtils.isNotBlank(pubId)) {
            device.setPublicId(pubId);
            return device;
        }
        delete(account.getUsername(), device.getId());
        return null;
    }

    /**
     * Decode yubikey registered device.
     *
     * @param devicePublicId the device public id
     * @return the string
     */
    protected String decodeYubikeyRegisteredDevice(final String devicePublicId) {
        try {
            return getCipherExecutor().decode(devicePublicId);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    /**
     * Gets account internal.
     *
     * @param username the username
     * @return the account internal
     */
    protected abstract YubiKeyAccount getAccountInternal(String username);

    /**
     * Gets accounts internal.
     *
     * @return the accounts internal
     */
    protected abstract Collection<? extends YubiKeyAccount> getAccountsInternal();
}
