package org.apereo.cas.adaptors.yubikey.dao;

import module java.base;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.YubiKeyDeviceRegistrationRequest;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.adaptors.yubikey.registry.BaseYubiKeyAccountRegistry;
import org.apereo.cas.util.RandomUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/**
 * This is {@link MongoDbYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class MongoDbYubiKeyAccountRegistry extends BaseYubiKeyAccountRegistry {
    private static final int MAXIMUM_IDENTIFIER_ATTEMPTS = 10;

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
        val query = new Query().addCriteria(Criteria.where(YubiKeyAccount.FIELD_USERNAME).is(uid));
        return mongoTemplate.findOne(query, MongoDbYubiKeyAccount.class, this.collectionName);
    }

    @Override
    public void delete(final String uid) {
        val query = new Query();
        query.addCriteria(Criteria.where(YubiKeyAccount.FIELD_USERNAME).is(uid));
        this.mongoTemplate.remove(query, MongoDbYubiKeyAccount.class, this.collectionName);
    }

    @Override
    public void delete(final String username, final long deviceId) {
        val account = getAccountInternal(username);
        if (account != null && account.getDevices().removeIf(device -> device.getId() == deviceId)) {
            update(account);
        }
    }

    @Override
    public void deleteAll() {
        this.mongoTemplate.remove(new Query(), MongoDbYubiKeyAccount.class, this.collectionName);
    }

    @Override
    public boolean update(final YubiKeyAccount account) {
        val query = new Query().addCriteria(Criteria.where(YubiKeyAccount.FIELD_USERNAME).is(account.getUsername()));
        val update = Update.update(YubiKeyAccount.FIELD_DEVICES, account.getDevices());
        this.mongoTemplate.updateFirst(query, update, MongoDbYubiKeyAccount.class, this.collectionName);
        return true;
    }

    @Override
    public YubiKeyAccount save(final YubiKeyDeviceRegistrationRequest request, final YubiKeyRegisteredDevice... device) {
        val result = MongoDbYubiKeyAccount.builder()
            .username(request.getUsername())
            .devices(Arrays.stream(device).collect(Collectors.toList()))
            .tenant(request.getTenant())
            .build();
        return insert(result, 0, true);
    }

    /**
     * Save the account. When the user already has an account, its document is replaced under the same identifier
     * rather than a second document being added for the same user. Otherwise the account is inserted, never saved over
     * another document: it keeps its own identifier when no document holds it, and is given a new one when one does,
     * so an account imported from elsewhere never replaces another user's account.
     *
     * @param account the account
     * @return the saved account
     */
    @Override
    public YubiKeyAccount save(final YubiKeyAccount account) {
        val existing = getAccountInternal(account.getUsername());
        val result = MongoDbYubiKeyAccount.builder()
            .username(account.getUsername())
            .devices(account.getDevices())
            .tenant(account.getTenant())
            .build();
        if (existing != null) {
            result.setId(existing.getId());
            return mongoTemplate.save(result, this.collectionName);
        }
        return insert(result, account.getId(), false);
    }

    /**
     * Insert a new account under the given identifier, or under a new random one when it is not set or a document
     * already holds it; the database refuses a duplicate identifier, so no other account is replaced. The username is
     * unique too, so when the same user's account was stored meanwhile, no second one is added: a registration adds its
     * devices to that account, and an imported account replaces it.
     *
     * @param account      the account
     * @param preferredId  the identifier to keep, if positive
     * @param addToCurrent whether to add the devices to an account stored meanwhile rather than replace it
     * @return the inserted account
     */
    private YubiKeyAccount insert(final MongoDbYubiKeyAccount account, final long preferredId, final boolean addToCurrent) {
        var id = preferredId > 0 ? preferredId : RandomUtils.nextLong(1, Long.MAX_VALUE);
        for (var attempt = 1; attempt <= MAXIMUM_IDENTIFIER_ATTEMPTS; attempt++) {
            try {
                account.setId(id);
                return mongoTemplate.insert(account, this.collectionName);
            } catch (final DuplicateKeyException e) {
                val current = getAccountInternal(account.getUsername());
                if (current != null && addToCurrent) {
                    LOGGER.debug("YubiKey account of [{}] was stored meanwhile; adding the devices to it", account.getUsername());
                    current.getDevices().addAll(account.getDevices());
                    update(current);
                    return current;
                }
                if (current != null) {
                    LOGGER.debug("YubiKey account of [{}] was stored meanwhile; replacing it", account.getUsername());
                    account.setId(current.getId());
                    return mongoTemplate.save(account, this.collectionName);
                }
                LOGGER.debug("YubiKey account identifier [{}] is taken; trying another", id);
                id = RandomUtils.nextLong(1, Long.MAX_VALUE);
            }
        }
        throw new IllegalStateException("Unable to find a free identifier for the YubiKey account of " + account.getUsername());
    }

    @Override
    protected Collection<? extends YubiKeyAccount> getAccountsInternal() {
        return this.mongoTemplate.findAll(MongoDbYubiKeyAccount.class, this.collectionName);
    }
}
