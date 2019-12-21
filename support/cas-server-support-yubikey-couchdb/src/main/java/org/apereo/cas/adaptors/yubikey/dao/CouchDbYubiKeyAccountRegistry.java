package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.registry.BaseYubiKeyAccountRegistry;
import org.apereo.cas.couchdb.yubikey.CouchDbYubiKeyAccount;
import org.apereo.cas.couchdb.yubikey.YubiKeyAccountCouchDbRepository;

import lombok.val;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    public boolean registerAccountFor(final String uid, final String token) {
        val accountValidator = getAccountValidator();
        if (accountValidator.isValid(uid, token)) {
            val publicKeyId = getCipherExecutor().encode(accountValidator.getTokenPublicId(token));
            var account = couchDb.findByUsername(uid);
            if (account == null) {
                couchDb.add(new CouchDbYubiKeyAccount(List.of(publicKeyId), uid));
            } else {
                account.registerDevice(publicKeyId);
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
                val devices = it.getDeviceIdentifiers().stream()
                    .map(pubId -> getCipherExecutor().decode(pubId))
                    .collect(Collectors.toCollection(ArrayList::new));
                it.setDeviceIdentifiers(devices);
            })
            .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Optional<YubiKeyAccount> getAccount(final String uid) {
        val account = couchDb.findByUsername(uid);
        if (account != null) {
            val devices = account.getDeviceIdentifiers().stream()
                .map(pubId -> getCipherExecutor().decode(pubId))
                .collect(Collectors.toCollection(ArrayList::new));
            val yubiKeyAccount = new YubiKeyAccount(account.getId(), devices, account.getUsername());
            return Optional.of(yubiKeyAccount);
        }
        return Optional.empty();
    }
}
