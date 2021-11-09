package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.adaptors.yubikey.registry.BaseYubiKeyAccountRegistry;
import org.apereo.cas.couchdb.yubikey.CouchDbYubiKeyAccount;
import org.apereo.cas.couchdb.yubikey.YubiKeyAccountCouchDbRepository;

import lombok.val;

import java.util.Arrays;
import java.util.Collection;
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
    public YubiKeyAccount save(final YubiKeyDeviceRegistrationRequest request,
                               final YubiKeyRegisteredDevice... device) {
        val account = CouchDbYubiKeyAccount.builder()
            .username(request.getUsername())
            .devices(Arrays.stream(device).collect(Collectors.toList()))
            .build();
        couchDb.add(account);
        return account;
    }

    @Override
    public YubiKeyAccount save(final YubiKeyAccount acct) {
        val account = CouchDbYubiKeyAccount.builder()
            .username(acct.getUsername())
            .devices(acct.getDevices())
            .build();
        couchDb.add(account);
        return account;
    }

    @Override
    public boolean update(final YubiKeyAccount account) {
        couchDb.update(CouchDbYubiKeyAccount.class.cast(account));
        return true;
    }

    @Override
    public Collection<? extends YubiKeyAccount> getAccountsInternal() {
        return couchDb.getAll();
    }

    @Override
    public YubiKeyAccount getAccountInternal(final String uid) {
        return couchDb.findByUsername(uid);
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
}
