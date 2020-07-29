package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.adaptors.yubikey.registry.BaseYubiKeyAccountRegistry;
import org.apereo.cas.couchdb.yubikey.CouchDbYubiKeyAccount;
import org.apereo.cas.couchdb.yubikey.YubiKeyAccountCouchDbRepository;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link CouchDbYubiKeyAccountRegistry}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public class CouchDbYubiKeyAccountRegistry extends BaseYubiKeyAccountRegistry {
    private final YubiKeyAccountCouchDbRepository couchDb;

    public CouchDbYubiKeyAccountRegistry(final YubiKeyAccountValidator accountValidator,
                                         final YubiKeyAccountCouchDbRepository couchDb) {
        super(accountValidator);
        this.couchDb = couchDb;
    }

    @Override
    public boolean registerAccountFor(final YubiKeyDeviceRegistrationRequest request) {
        val accountValidator = getAccountValidator();
        if (accountValidator.isValid(request.getUsername(), request.getToken())) {
            val yubikeyPublicId = getCipherExecutor().encode(accountValidator.getTokenPublicId(request.getToken()));

            val device = YubiKeyRegisteredDevice.builder()
                .id(System.currentTimeMillis())
                .name(request.getName())
                .publicId(yubikeyPublicId)
                .registrationDate(ZonedDateTime.now(Clock.systemUTC()))
                .build();

            var account = couchDb.findByUsername(request.getUsername());
            if (account == null) {
                account = CouchDbYubiKeyAccount.builder()
                    .username(request.getUsername())
                    .devices(CollectionUtils.wrapList(device))
                    .build();
                couchDb.add(account);
            } else {
                account.getDevices().add(device);
                couchDb.update(account);
            }
            return true;
        }
        return false;
    }

    @Override
    public Collection<YubiKeyAccount> getAccounts() {
        return couchDb.getAll()
            .stream()
            .peek(it -> {
                val devices = it.getDevices().stream()
                    .filter(device -> getCipherExecutor().decode(device.getPublicId()) != null)
                    .collect(Collectors.toCollection(ArrayList::new));
                it.setDevices(devices);
            })
            .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Optional<YubiKeyAccount> getAccount(final String uid) {
        val account = couchDb.findByUsername(uid);
        if (account != null) {
            val devices = account.getDevices().stream()
                .filter(device -> getCipherExecutor().decode(device.getPublicId()) != null)
                .collect(Collectors.toCollection(ArrayList::new));
            account.setDevices(devices);
            return Optional.of(account);
        }
        return Optional.empty();
    }
}
