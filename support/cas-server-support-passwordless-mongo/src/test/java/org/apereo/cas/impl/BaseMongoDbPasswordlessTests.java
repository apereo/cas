package org.apereo.cas.impl;

import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.config.CasMongoDbPasswordlessAuthenticationAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import lombok.Getter;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;

/**
 * This is {@link BaseMongoDbPasswordlessTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = {
    CasMongoDbPasswordlessAuthenticationAutoConfiguration.class,
    BasePasswordlessUserAccountStoreTests.SharedTestConfiguration.class
},
    properties = {
        "cas.authn.passwordless.accounts.mongo.host=localhost",
        "cas.authn.passwordless.accounts.mongo.port=27017",
        "cas.authn.passwordless.accounts.mongo.drop-collection=true",
        "cas.authn.passwordless.accounts.mongo.collection=PasswordlessAccounts",
        "cas.authn.passwordless.accounts.mongo.user-id=root",
        "cas.authn.passwordless.accounts.mongo.password=secret",
        "cas.authn.passwordless.accounts.mongo.database-name=audit",
        "cas.authn.passwordless.accounts.mongo.authentication-database-name=admin",

        "cas.authn.passwordless.tokens.mongo.host=localhost",
        "cas.authn.passwordless.tokens.mongo.port=27017",
        "cas.authn.passwordless.tokens.mongo.drop-collection=true",
        "cas.authn.passwordless.tokens.mongo.collection=PasswordlessAccounts",
        "cas.authn.passwordless.tokens.mongo.user-id=root",
        "cas.authn.passwordless.tokens.mongo.password=secret",
        "cas.authn.passwordless.tokens.mongo.database-name=audit",
        "cas.authn.passwordless.tokens.mongo.authentication-database-name=admin"
    })
@Getter
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
public abstract class BaseMongoDbPasswordlessTests {
    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(PasswordlessUserAccountStore.BEAN_NAME)
    protected PasswordlessUserAccountStore passwordlessUserAccountStore;

    @Autowired
    @Qualifier(PasswordlessTokenRepository.BEAN_NAME)
    protected PasswordlessTokenRepository passwordlessTokenRepository;

    @Autowired
    @Qualifier("mongoDbPasswordlessAuthenticationTemplate")
    protected MongoOperations mongoDbTemplate;

    @Autowired
    @Qualifier("mongoDbPasswordlessAuthenticationTokensTemplate")
    protected MongoOperations mongoDbPasswordlessAuthenticationTokensTemplate;
}
