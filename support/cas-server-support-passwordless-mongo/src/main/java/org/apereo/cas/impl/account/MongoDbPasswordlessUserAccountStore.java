package org.apereo.cas.impl.account;

import module java.base;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountCustomizer;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.configuration.model.support.passwordless.account.PasswordlessAuthenticationMongoDbAccountsProperties;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/**
 * This is {@link MongoDbPasswordlessUserAccountStore}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
public class MongoDbPasswordlessUserAccountStore implements PasswordlessUserAccountStore {
    protected final MongoOperations mongoTemplate;

    protected final PasswordlessAuthenticationMongoDbAccountsProperties properties;

    protected final ConfigurableApplicationContext applicationContext;

    protected final List<PasswordlessUserAccountCustomizer> customizerList;
    
    @Override
    public Optional<PasswordlessUserAccount> findUser(final PasswordlessAuthenticationRequest request) {
        val query = new Query().addCriteria(Criteria.where("username").is(request.getUsername()));
        val account = mongoTemplate.findOne(query, PasswordlessUserAccount.class, properties.getCollection());
        val result = Optional.ofNullable(account);
        customizerList
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .forEach(customizer -> customizer.customize(result));
        return result;
    }
}
