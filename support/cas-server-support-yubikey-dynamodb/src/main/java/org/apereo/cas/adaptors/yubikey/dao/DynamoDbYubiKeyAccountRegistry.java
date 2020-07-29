package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.adaptors.yubikey.registry.BaseYubiKeyAccountRegistry;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link DynamoDbYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class DynamoDbYubiKeyAccountRegistry extends BaseYubiKeyAccountRegistry {
    private final YubiKeyDynamoDbFacilitator dynamoDbFacilitator;

    public DynamoDbYubiKeyAccountRegistry(final YubiKeyAccountValidator accountValidator,
                                          final YubiKeyDynamoDbFacilitator dynamoDbFacilitator) {
        super(accountValidator);
        this.dynamoDbFacilitator = dynamoDbFacilitator;
    }

    @Override
    public boolean registerAccountFor(final YubiKeyDeviceRegistrationRequest request) {
        val accountValidator = getAccountValidator();
        if (accountValidator.isValid(request.getUsername(), request.getToken())) {
            val yubikeyPublicId = getCipherExecutor().encode(accountValidator.getTokenPublicId(request.getToken()));

            val results = dynamoDbFacilitator.getAccounts(request.getUsername());

            val device = YubiKeyRegisteredDevice.builder()
                .id(System.currentTimeMillis())
                .name(request.getName())
                .publicId(yubikeyPublicId)
                .registrationDate(ZonedDateTime.now(Clock.systemUTC()))
                .build();

            if (results.isEmpty()) {
                val account = YubiKeyAccount.builder()
                    .username(request.getUsername())
                    .devices(CollectionUtils.wrapList(device))
                    .build();
                return dynamoDbFacilitator.save(account);
            }
            val account = results.get(0);
            account.getDevices().add(device);
            return dynamoDbFacilitator.update(account);
        }
        return false;
    }

    @Override
    public Collection<? extends YubiKeyAccount> getAccounts() {
        return dynamoDbFacilitator.getAccounts()
            .stream()
            .peek(it -> {
                val devices = it.getDevices().stream()
                    .filter(device -> getCipherExecutor().decode(device.getPublicId()) != null)
                    .collect(Collectors.toCollection(ArrayList::new));
                it.setDevices(devices);
            })
            .collect(Collectors.toList());
    }

    @Override
    public Optional<? extends YubiKeyAccount> getAccount(final String uid) {
        val accounts = dynamoDbFacilitator.getAccounts(uid);
        if (accounts.isEmpty()) {
            return Optional.empty();
        }
        val account = accounts.iterator().next();
        val devices = account.getDevices()
            .stream()
            .map(device -> device.setPublicId(getCipherExecutor().decode(device.getPublicId())))
            .collect(Collectors.toCollection(ArrayList::new));
        account.setDevices(devices);
        return Optional.of(account);
    }

    @Override
    public void delete(final String uid) {
        dynamoDbFacilitator.delete(uid);
    }

    @Override
    public void deleteAll() {
        dynamoDbFacilitator.removeDevices();
    }
}
