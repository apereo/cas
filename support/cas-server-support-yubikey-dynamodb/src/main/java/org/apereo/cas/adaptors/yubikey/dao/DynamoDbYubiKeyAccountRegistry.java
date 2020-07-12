package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.registry.BaseYubiKeyAccountRegistry;

import lombok.val;

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
    public boolean registerAccountFor(final String uid, final String token) {
        val accountValidator = getAccountValidator();
        if (accountValidator.isValid(uid, token)) {
            val yubikeyPublicId = getCipherExecutor().encode(accountValidator.getTokenPublicId(token));

            val results = getAccount(uid);
            val account = results.isEmpty() ? new YubiKeyAccount() : results.get();
            account.registerDevice(yubikeyPublicId);
            account.setUsername(uid);
            if (results.isEmpty()) {
                return dynamoDbFacilitator.save(account);
            }
            return dynamoDbFacilitator.update(account);
        }
        return false;
    }

    @Override
    public Collection<? extends YubiKeyAccount> getAccounts() {
        return dynamoDbFacilitator.getAccounts()
            .stream()
            .peek(it -> {
                val devices = it.getDeviceIdentifiers().stream()
                    .map(pubId -> getCipherExecutor().decode(pubId))
                    .collect(Collectors.toCollection(ArrayList::new));
                it.setDeviceIdentifiers(devices);
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
        val devices = account.getDeviceIdentifiers().stream()
            .map(pubId -> getCipherExecutor().decode(pubId))
            .collect(Collectors.toCollection(ArrayList::new));
        val yubiAccount = YubiKeyAccount.builder()
            .id(account.getId())
            .username(account.getUsername())
            .deviceIdentifiers(devices)
            .build();
        return Optional.of(yubiAccount);
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
