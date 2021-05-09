package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.adaptors.yubikey.registry.BaseYubiKeyAccountRegistry;

import lombok.val;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Arrays;
import java.util.Collection;
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
    public YubiKeyAccount getAccountInternal(final String uid) {
        val query = new Query().addCriteria(Criteria.where(MongoDbYubiKeyAccount.FIELD_USERNAME).is(uid));
        return mongoTemplate.findOne(query, MongoDbYubiKeyAccount.class, this.collectionName);
    }

    @Override
    public void delete(final String uid) {
        val query = new Query();
        query.addCriteria(Criteria.where(MongoDbYubiKeyAccount.FIELD_USERNAME).is(uid));
        this.mongoTemplate.remove(query, MongoDbYubiKeyAccount.class, this.collectionName);
    }

    @Override
    public void delete(final String username, final long deviceId) {
        val account = getAccountInternal(username);
        if (account.getDevices().removeIf(device -> device.getId() == deviceId)) {
            update(account);
        }
    }

    @Override
    public void deleteAll() {
        this.mongoTemplate.remove(new Query(), MongoDbYubiKeyAccount.class, this.collectionName);
    }

    @Override
    public boolean update(final YubiKeyAccount account) {
        val query = new Query().addCriteria(Criteria.where(MongoDbYubiKeyAccount.FIELD_USERNAME).is(account.getUsername()));
        val update = Update.update(MongoDbYubiKeyAccount.FIELD_DEVICES, account.getDevices());
        this.mongoTemplate.updateFirst(query, update, MongoDbYubiKeyAccount.class, this.collectionName);
        return true;
    }

    @Override
    public YubiKeyAccount save(final YubiKeyDeviceRegistrationRequest request, final YubiKeyRegisteredDevice... device) {
        val result = MongoDbYubiKeyAccount.builder()
            .id(System.currentTimeMillis())
            .username(request.getUsername())
            .devices(Arrays.stream(device).collect(Collectors.toList()))
            .build();
        return mongoTemplate.save(result, this.collectionName);
    }

    @Override
    public YubiKeyAccount save(final YubiKeyAccount account) {
        val result = MongoDbYubiKeyAccount.builder()
            .id(System.currentTimeMillis())
            .username(account.getUsername())
            .devices(account.getDevices())
            .build();
        return mongoTemplate.save(result, this.collectionName);
    }

    @Override
    protected Collection<? extends YubiKeyAccount> getAccountsInternal() {
        return this.mongoTemplate.findAll(MongoDbYubiKeyAccount.class, this.collectionName);
    }
}
