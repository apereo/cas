package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.adaptors.yubikey.registry.BaseYubiKeyAccountRegistry;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Clock;
import java.time.ZonedDateTime;
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

            val query = new Query().addCriteria(Criteria.where(MongoDbYubiKeyAccount.FIELD_USERNAME).is(request.getUsername()));
            var account = this.mongoTemplate.findOne(query, MongoDbYubiKeyAccount.class, this.collectionName);
            if (account == null) {
                account = MongoDbYubiKeyAccount.builder()
                    .id(System.currentTimeMillis())
                    .username(request.getUsername())
                    .devices(CollectionUtils.wrapList(device))
                    .build();
                this.mongoTemplate.save(account, this.collectionName);
                return true;
            }
            account.getDevices().add(device);
            val update = Update.update("devices", account.getDevices());
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
                val devices = it.getDevices()
                    .stream()
                    .map(device -> device.setPublicId(getCipherExecutor().decode(device)))
                    .collect(Collectors.toCollection(ArrayList::new));
                it.setDevices(devices);
            })
            .collect(Collectors.toList());
    }

    @Override
    public Optional<? extends YubiKeyAccount> getAccount(final String uid) {
        val query = new Query().addCriteria(Criteria.where(MongoDbYubiKeyAccount.FIELD_USERNAME).is(uid));
        val account = this.mongoTemplate.findOne(query, MongoDbYubiKeyAccount.class, this.collectionName);
        if (account != null) {
            val devices = account.getDevices()
                .stream()
                .map(device -> device.setPublicId(getCipherExecutor().decode(device.getPublicId())))
                .collect(Collectors.toCollection(ArrayList::new));
            account.setDevices(devices);
            return Optional.of(account);
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
