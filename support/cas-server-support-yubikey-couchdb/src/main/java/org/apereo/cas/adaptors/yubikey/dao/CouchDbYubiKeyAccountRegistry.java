package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.adaptors.yubikey.registry.BaseYubiKeyAccountRegistry;
import org.apereo.cas.couchdb.yubikey.CouchDbYubiKeyAccount;
import org.apereo.cas.couchdb.yubikey.YubiKeyAccountCouchDbRepository;
import org.apereo.cas.util.LoggingUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link CouchDbYubiKeyAccountRegistry}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Slf4j
public class CouchDbYubiKeyAccountRegistry extends BaseYubiKeyAccountRegistry {
    private final YubiKeyAccountCouchDbRepository couchDb;

    public CouchDbYubiKeyAccountRegistry(final YubiKeyAccountValidator accountValidator,
                                         final YubiKeyAccountCouchDbRepository couchDb) {
        super(accountValidator);
        this.couchDb = couchDb;
    }

    @Override
    protected YubiKeyAccount saveAccount(final YubiKeyDeviceRegistrationRequest request,
                                         final YubiKeyRegisteredDevice... device) {
        val account = CouchDbYubiKeyAccount.builder()
            .username(request.getUsername())
            .devices(Arrays.stream(device).collect(Collectors.toList()))
            .build();
        couchDb.add(account);
        return account;
    }

    @Override
    protected boolean update(final YubiKeyAccount account) {
        couchDb.update(CouchDbYubiKeyAccount.class.cast(account));
        return true;
    }

    @Override
    public Collection<? extends YubiKeyAccount> getAccountsInternal() {
        return couchDb.getAll();
    }

    @Override
    public Optional<YubiKeyAccount> getAccount(final String uid) {
        val account = couchDb.findByUsername(uid);
        if (account != null) {
            return toYubiKeyAccount(account);
        }
        return Optional.empty();
    }

    @Override
    public void delete(final String uid) {
        val account = couchDb.findByUsername(uid);
        if (account != null) {
            couchDb.remove(account);
        }
    }

    @Override
    public void delete(final String username, final long deviceId) {
        couchDb.remove(username, deviceId);
    }

    @Override
    public void deleteAll() {
        couchDb.removeAll();
    }

    private Optional<YubiKeyAccount> toYubiKeyAccount(final CouchDbYubiKeyAccount account) {
        val devices = account.getDevices()
            .stream()
            .map(device -> {
                try {
                    val pubId = getCipherExecutor().decode(device.getPublicId());
                    device.setPublicId(pubId);
                    return device;
                } catch (final Exception e) {
                    LoggingUtils.error(LOGGER, e);
                    delete(account.getUsername(), device.getId());
                }
                return null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(ArrayList::new));
        account.setDevices(devices);
        return Optional.of(account);
    }
}
