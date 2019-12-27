package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.registry.BaseYubiKeyAccountRegistry;

import lombok.val;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link MongoDbYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class MongoDbYubiKeyAccountRegistry extends BaseYubiKeyAccountRegistry {

    private final String collectionName;

    private final MongoOperations mongoTemplate;

    public MongoDbYubiKeyAccountRegistry(final YubiKeyAccountValidator accountValidator,
                                         final MongoOperations mongoTemplate,
                                         final String collectionName) {
        super(accountValidator);
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
    }

    @Override
    public boolean registerAccountFor(final String uid, final String token) {
        val accountValidator = getAccountValidator();
        if (accountValidator.isValid(uid, token)) {
            val yubikeyPublicId = getCipherExecutor().encode(accountValidator.getTokenPublicId(token));

            val query = new Query().addCriteria(Criteria.where(MongoDbYubiKeyAccount.FIELD_USERNAME).is(uid));
            var account = this.mongoTemplate.findOne(query, MongoDbYubiKeyAccount.class, this.collectionName);
            if (account == null) {
                account = new MongoDbYubiKeyAccount();
                account.setUsername(uid);
                account.registerDevice(yubikeyPublicId);
                this.mongoTemplate.save(account, this.collectionName);
                return true;
            }
            account.registerDevice(yubikeyPublicId);
            val update = Update.update(MongoDbYubiKeyAccount.FIELD_DEVICE_IDENTIFIERS, account.getDeviceIdentifiers());
            this.mongoTemplate.updateFirst(query, update, MongoDbYubiKeyAccount.class, this.collectionName);
            return true;
        }
        return false;
    }

    @Override
    public Collection<? extends YubiKeyAccount> getAccounts() {
        return this.mongoTemplate.findAll(MongoDbYubiKeyAccount.class, this.collectionName)
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
        val query = new Query().addCriteria(Criteria.where(MongoDbYubiKeyAccount.FIELD_USERNAME).is(uid));
        val account = this.mongoTemplate.findOne(query, MongoDbYubiKeyAccount.class, this.collectionName);
        if (account != null) {
            val devices = account.getDeviceIdentifiers().stream()
                .map(pubId -> getCipherExecutor().decode(pubId))
                .collect(Collectors.toCollection(ArrayList::new));
            val yubiAccount = new MongoDbYubiKeyAccount(account.getId(), devices, account.getUsername());
            return Optional.of(yubiAccount);
        }
        return Optional.empty();
    }

    @Override
    public void delete(final String uid) {
        val query = new Query();
        query.addCriteria(Criteria.where(MongoDbYubiKeyAccount.FIELD_USERNAME).is(uid));
        this.mongoTemplate.remove(query, MongoDbYubiKeyAccount.class, this.collectionName);
    }

    @Override
    public void deleteAll() {
        this.mongoTemplate.remove(new Query(), MongoDbYubiKeyAccount.class, this.collectionName);
    }
}
