package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.registry.BaseYubiKeyAccountRegistry;

import lombok.val;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collection;
import java.util.Optional;

import static java.util.stream.Collectors.*;

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
                                         final MongoOperations mongoTemplate, final String collectionName) {
        super(accountValidator);
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
    }

    @Override
    public boolean registerAccountFor(final String uid, final String token) {
        if (getAccountValidator().isValid(uid, token)) {
            val yubikeyPublicId = getAccountValidator().getTokenPublicId(token);
            val account = new YubiKeyAccount();
            account.setPublicId(getCipherExecutor().encode(yubikeyPublicId));
            account.setUsername(uid);
            this.mongoTemplate.save(account, this.collectionName);
            return true;
        }
        return false;
    }

    @Override
    public Collection<? extends YubiKeyAccount> getAccounts() {
        return this.mongoTemplate.findAll(YubiKeyAccount.class, this.collectionName)
            .stream()
            .peek(it -> it.setPublicId(getCipherExecutor().decode(it.getPublicId())))
            .collect(toList());
    }

    @Override
    public Optional<? extends YubiKeyAccount> getAccount(final String uid) {
        val query = new Query();
        query.addCriteria(Criteria.where("username").is(uid));
        val account = this.mongoTemplate.findOne(query, YubiKeyAccount.class, this.collectionName);
        if (account != null) {
            return Optional.of(new YubiKeyAccount(account.getId(), getCipherExecutor().decode(account.getPublicId()), account.getUsername()));
        }
        return Optional.empty();
    }

    @Override
    public void delete(final String uid) {
        val query = new Query();
        query.addCriteria(Criteria.where("username").is(uid));
        this.mongoTemplate.remove(query, YubiKeyAccount.class, this.collectionName);
    }

    @Override
    public void deleteAll() {
        this.mongoTemplate.remove(new Query(), YubiKeyAccount.class, this.collectionName);
    }
}
