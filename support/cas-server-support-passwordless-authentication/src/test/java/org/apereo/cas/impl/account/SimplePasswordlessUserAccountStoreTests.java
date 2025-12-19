package org.apereo.cas.impl.account;

import module java.base;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.impl.BasePasswordlessUserAccountStoreTests;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulPasswordlessUserAccountStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = {
    "cas.authn.passwordless.accounts.simple.casuser=1234567890",
    "cas.authn.passwordless.tokens.crypto.enabled=false"
})
@Tag("Simple")
class SimplePasswordlessUserAccountStoreTests extends BasePasswordlessUserAccountStoreTests {
    @Autowired
    @Qualifier(PasswordlessUserAccountStore.BEAN_NAME)
    private PasswordlessUserAccountStore passwordlessUserAccountStore;

    @Test
    void verifyAction() throws Throwable {
        assertTrue(passwordlessUserAccountStore.findUser(PasswordlessAuthenticationRequest
            .builder()
            .username("casuser")
            .build()).isPresent());
        assertTrue(passwordlessUserAccountStore.findUser(PasswordlessAuthenticationRequest
            .builder()
            .username("other")
            .build()).isEmpty());
    }
}
