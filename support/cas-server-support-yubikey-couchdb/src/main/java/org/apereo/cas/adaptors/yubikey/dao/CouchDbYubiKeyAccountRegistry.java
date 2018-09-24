package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.registry.BaseYubiKeyAccountRegistry;
import org.apereo.cas.couchdb.yubikey.CouchDbYubiKeyAccount;
import org.apereo.cas.couchdb.yubikey.YubiKeyAccountCouchDbRepository;

import lombok.val;

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
    private YubiKeyAccountCouchDbRepository couchDb;

    public CouchDbYubiKeyAccountRegistry(final YubiKeyAccountValidator accountValidator, final YubiKeyAccountCouchDbRepository couchDb) {
        super(accountValidator);
        this.couchDb = couchDb;
    }

    @Override
    public boolean registerAccountFor(final String uid, final String token) {
        if (getAccountValidator().isValid(uid, token)) {
            couchDb.add(new CouchDbYubiKeyAccount(getCipherExecutor().encode(getAccountValidator().getTokenPublicId(token)), uid));
            return true;
        }
        return false;
    }

    @Override
    public Collection<YubiKeyAccount> getAccounts() {
        return couchDb.getAll().stream().map(it -> {
            it.setPublicId(getCipherExecutor().decode(it.getPublicId()));
            return (YubiKeyAccount) it;
        }).collect(Collectors.toList());
    }

    @Override
    public Optional<YubiKeyAccount> getAccount(final String uid) {
        val account = couchDb.findByUsername(uid);
        if (account != null) {
            return Optional.of(new YubiKeyAccount(account.getId(), getCipherExecutor().decode(account.getPublicId()), account.getUsername()));
        }
        return Optional.empty();
    }
}
