package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.adaptors.yubikey.registry.BaseYubiKeyAccountRegistry;

import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
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
    public Collection<? extends YubiKeyAccount> getAccountsInternal() {
        return dynamoDbFacilitator.getAccounts();
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
    public void delete(final String username, final long deviceId) {
        dynamoDbFacilitator.delete(username, deviceId);
    }

    @Override
    public void delete(final String uid) {
        dynamoDbFacilitator.delete(uid);
    }

    @Override
    public void deleteAll() {
        dynamoDbFacilitator.removeDevices();
    }

    @Override
    protected YubiKeyAccount saveAccount(final YubiKeyDeviceRegistrationRequest request,
                                         final YubiKeyRegisteredDevice... device) {
        val account = YubiKeyAccount.builder()
            .username(request.getUsername())
            .devices(Arrays.stream(device).collect(Collectors.toList()))
            .build();
        if (dynamoDbFacilitator.save(account)) {
            return account;
        }
        return null;
    }

    @Override
    protected boolean update(final YubiKeyAccount account) {
        return dynamoDbFacilitator.update(account);
    }
}
