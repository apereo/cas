package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PasswordlessCasWebflowLoginContextProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("WebflowAuthenticationActions")
class PasswordlessCasWebflowLoginContextProviderTests extends BasePasswordlessAuthenticationActionTests {
    @Autowired
    @Qualifier("passwordlessCasWebflowLoginContextProvider")
    private CasWebflowLoginContextProvider passwordlessCasWebflowLoginContextProvider;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val account = new PasswordlessUserAccount();
        account.setUsername(UUID.randomUUID().toString());
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);

        val results = passwordlessCasWebflowLoginContextProvider.getCandidateUsername(context);
        assertFalse(results.isEmpty());
        assertEquals(account.getUsername(), results.get());

        assertTrue(passwordlessCasWebflowLoginContextProvider.isLoginFormUsernameInputDisabled(context));
        assertFalse(passwordlessCasWebflowLoginContextProvider.isLoginFormUsernameInputVisible(context));
    }

}
