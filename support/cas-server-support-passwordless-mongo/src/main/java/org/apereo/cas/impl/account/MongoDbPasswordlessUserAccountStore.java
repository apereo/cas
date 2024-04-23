package org.apereo.cas.impl.account;

import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.configuration.model.support.passwordless.account.PasswordlessAuthenticationMongoDbAccountsProperties;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Optional;

/**
 * This is {@link MongoDbPasswordlessUserAccountStore}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
public class MongoDbPasswordlessUserAccountStore implements PasswordlessUserAccountStore {
    private final MongoOperations mongoTemplate;

    private final PasswordlessAuthenticationMongoDbAccountsProperties properties;

    @Override
    public Optional<PasswordlessUserAccount> findUser(final PasswordlessAuthenticationRequest request) {
        val query = new Query().addCriteria(Criteria.where("username").is(request.getUsername()));
        val account = this.mongoTemplate.findOne(query, PasswordlessUserAccount.class, properties.getCollection());
        return Optional.ofNullable(account);
    }
}
