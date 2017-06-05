package org.apereo.cas.adaptors.yubikey.dao;

import com.yubico.client.v2.YubicoClient;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.registry.BaseYubiKeyAccountRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.Assert;

/**
 * This is {@link MongoDbYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class MongoDbYubiKeyAccountRegistry extends BaseYubiKeyAccountRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbYubiKeyAccountRegistry.class);
    private final String collectionName;
    private final MongoOperations mongoTemplate;

    public MongoDbYubiKeyAccountRegistry(final YubiKeyAccountValidator accountValidator,
                                         final MongoOperations mongoTemplate, final String collectionName,
                                         final boolean dropCollection) {
        super(accountValidator);

        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;

        Assert.notNull(this.mongoTemplate);

        if (dropCollection) {
            LOGGER.debug("Dropping database collection: [{}]", this.collectionName);
            this.mongoTemplate.dropCollection(this.collectionName);
        }

        if (!this.mongoTemplate.collectionExists(this.collectionName)) {
            LOGGER.debug("Creating database collection: [{}]", this.collectionName);
            this.mongoTemplate.createCollection(this.collectionName);
        }
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid) {
        final Query query = new Query();
        query.addCriteria(Criteria.where("username").is(uid));
        return this.mongoTemplate.count(query, YubiKeyAccount.class, this.collectionName) > 0;
    }

    @Override
    public boolean registerAccountFor(final String uid, final String token) {
        if (accountValidator.isValid(uid, token)) {
            final String yubikeyPublicId = YubicoClient.getPublicId(token);
            final YubiKeyAccount account = new YubiKeyAccount();
            account.setPublicId(yubikeyPublicId);
            account.setUsername(uid);

            this.mongoTemplate.save(account, this.collectionName);
        }
        return false;
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid, final String yubikeyPublicId) {
        final Query query = new Query();
        query.addCriteria(Criteria.where("username").is(uid).and("publicId").is(yubikeyPublicId));
        return this.mongoTemplate.count(query, YubiKeyAccount.class, this.collectionName) > 0;
    }
}
