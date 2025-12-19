package org.apereo.cas.impl.account;

import module java.base;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.impl.BaseMongoDbPasswordlessTests;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MongoDbPasswordlessUserAccountStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("MongoDb")
@EnabledIfListeningOnPort(port = 27017)
class MongoDbPasswordlessUserAccountStoreTests extends BaseMongoDbPasswordlessTests {

    @Test
    void verifyAction() throws Throwable {
        val account = PasswordlessUserAccount.builder()
            .email("passwordlessuser@example.org")
            .phone("1234567890")
            .username("passwordlessuser")
            .name("CAS")
            .attributes(Map.of("lastName", List.of("Smith")))
            .build();
        val mongo = casProperties.getAuthn().getPasswordless().getAccounts().getMongo();
        mongoDbTemplate.save(account, mongo.getCollection());

        val user = passwordlessUserAccountStore.findUser(
            PasswordlessAuthenticationRequest
                .builder()
                .username("passwordlessuser")
                .build());
        assertTrue(user.isPresent());
        assertEquals("passwordlessuser@example.org", user.get().getEmail());
        assertEquals("1234567890", user.get().getPhone());
    }
}
