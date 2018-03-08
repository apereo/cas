package org.apereo.cas.adaptors.yubikey.dao;

import com.yubico.client.v2.YubicoClient;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyAccountValidator;
import org.apereo.cas.adaptors.yubikey.registry.BaseYubiKeyAccountRegistry;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Collection;

import static java.util.stream.Collectors.toList;

/**
 * This is {@link MongoDbYubiKeyAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
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
    public boolean isYubiKeyRegisteredFor(final String uid) {
        final Query query = new Query();
        query.addCriteria(Criteria.where("username").is(uid));
        return this.mongoTemplate.count(query, YubiKeyAccount.class, this.collectionName) > 0;
    }

    @Override
    public boolean isYubiKeyRegisteredFor(final String uid, final String yubikeyPublicId) {
        final Query query = new Query();
        query.addCriteria(Criteria.where("username").is(uid).and("publicId").is(getCipherExecutor().encode(yubikeyPublicId)));
        return this.mongoTemplate.count(query, YubiKeyAccount.class, this.collectionName) > 0;
    }

    @Override
    public boolean registerAccountFor(final String uid, final String token) {
        if (accountValidator.isValid(uid, token)) {
            final String yubikeyPublicId = YubicoClient.getPublicId(token);
            final YubiKeyAccount account = new YubiKeyAccount();
            account.setPublicId(getCipherExecutor().encode(yubikeyPublicId));
            account.setUsername(uid);

            this.mongoTemplate.save(account, this.collectionName);
            return true;
        }
        return false;
    }

    @Override
    public Collection<YubiKeyAccount> getAccounts() {
        return this.mongoTemplate.findAll(YubiKeyAccount.class, this.collectionName)
                .stream()
                .map(it -> {
                    it.setPublicId(getCipherExecutor().decode(it.getPublicId()));
                    return it;
                })
                .collect(toList());
    }
}
