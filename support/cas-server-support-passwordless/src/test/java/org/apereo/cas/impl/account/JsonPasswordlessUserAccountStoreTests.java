package org.apereo.cas.impl.account;

import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.impl.BasePasswordlessUserAccountStoreTests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyPasswordlessUserAccountStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = "cas.authn.passwordless.accounts.json.location=classpath:PasswordlessAccount.json")
@Tag("FileSystem")
public class JsonPasswordlessUserAccountStoreTests extends BasePasswordlessUserAccountStoreTests {

    @Autowired
    @Qualifier("passwordlessUserAccountStore")
    private PasswordlessUserAccountStore passwordlessUserAccountStore;

    @Test
    public void verifyAction() {
        val user = passwordlessUserAccountStore.findUser("casuser");
        assertTrue(user.isPresent());
    }
}
