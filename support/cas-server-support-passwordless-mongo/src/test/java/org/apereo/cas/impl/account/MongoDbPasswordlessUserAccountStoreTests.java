package org.apereo.cas.impl.account;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.config.MongoDbPasswordlessAuthenticationConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.impl.BasePasswordlessUserAccountStoreTests;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MongoDbPasswordlessUserAccountStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@TestPropertySource(properties = {
    "cas.authn.passwordless.accounts.mongo.host=localhost",
    "cas.authn.passwordless.accounts.mongo.port=27017",
    "cas.authn.passwordless.accounts.mongo.drop-collection=true",
    "cas.authn.passwordless.accounts.mongo.collection=PasswordlessAccounts",
    "cas.authn.passwordless.accounts.mongo.user-id=root",
    "cas.authn.passwordless.accounts.mongo.password=secret",
    "cas.authn.passwordless.accounts.mongo.database-name=audit",
    "cas.authn.passwordless.accounts.mongo.authentication-database-name=admin"
})
@Tag("MongoDb")
@Getter
@EnabledIfPortOpen(port = 27017)
@Import(MongoDbPasswordlessAuthenticationConfiguration.class)
public class MongoDbPasswordlessUserAccountStoreTests extends BasePasswordlessUserAccountStoreTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("passwordlessUserAccountStore")
    private PasswordlessUserAccountStore passwordlessUserAccountStore;

    @Autowired
    @Qualifier("mongoDbPasswordlessAuthenticationTemplate")
    private MongoTemplate mongoDbTemplate;

    @Test
    public void verifyAction() {
        val account = PasswordlessUserAccount.builder()
            .email("passwordlessuser@example.org")
            .phone("1234567890")
            .username("passwordlessuser")
            .name("CAS")
            .attributes(Map.of("lastName", List.of("Smith")))
            .build();
        val mongo = casProperties.getAuthn().getPasswordless().getAccounts().getMongo();
        this.mongoDbTemplate.save(account, mongo.getCollection());

        val user = passwordlessUserAccountStore.findUser("passwordlessuser");
        assertTrue(user.isPresent());
        assertEquals("passwordlessuser@example.org", user.get().getEmail());
        assertEquals("1234567890", user.get().getPhone());
    }
}
