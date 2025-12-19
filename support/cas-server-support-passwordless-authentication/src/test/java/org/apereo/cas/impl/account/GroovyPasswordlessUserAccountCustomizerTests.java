package org.apereo.cas.impl.account;

import module java.base;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.impl.BasePasswordlessUserAccountStoreTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyPasswordlessUserAccountCustomizerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@TestPropertySource(properties = {
    "cas.authn.passwordless.accounts.json.location=classpath:PasswordlessAccount.json",
    "cas.authn.passwordless.core.passwordless-account-customizer-script.location=classpath:PasswordlessAccountCustomizer.groovy"
})
@Tag("GroovyMfa")
class GroovyPasswordlessUserAccountCustomizerTests extends BasePasswordlessUserAccountStoreTests {
    @Autowired
    @Qualifier(PasswordlessUserAccountStore.BEAN_NAME)
    private PasswordlessUserAccountStore passwordlessUserAccountStore;

    @Test
    void verifyAction() throws Throwable {
        passwordlessUserAccountStore.reload();
        val user = passwordlessUserAccountStore.findUser(PasswordlessAuthenticationRequest
            .builder()
            .username("casuser")
            .build());
        assertTrue(user.isPresent());

        val userAccount = user.get();
        assertTrue(userAccount.isRequestPassword());
        assertSame(TriStateBoolean.TRUE, userAccount.getMultifactorAuthenticationEligible());
        assertEquals(List.of("CasClient"), userAccount.getAllowedDelegatedClients());
        assertEquals(List.of("Smith", "Smithson"), userAccount.getAttributes().get("lastName"));
    }
}
