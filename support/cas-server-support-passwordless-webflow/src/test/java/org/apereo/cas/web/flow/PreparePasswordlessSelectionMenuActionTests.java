package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PreparePasswordlessSelectionMenuActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("WebflowAuthenticationActions")
class PreparePasswordlessSelectionMenuActionTests extends BasePasswordlessAuthenticationActionTests {

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_PASSWORDLESS_PREPARE_SELECTION_MENU)
    private Action prepareLoginAction;
    
    @Test
    void verifySelectionMenu() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val account = PasswordlessUserAccount.builder()
            .username("casuser")
            .name("casuser")
            .multifactorAuthenticationEligible(TriStateBoolean.TRUE)
            .delegatedAuthenticationEligible(TriStateBoolean.TRUE)
            .build();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
        assertThrows(IllegalStateException.class, () -> prepareLoginAction.execute(context));

        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account.withAllowSelectionMenu(true));
        assertNull(prepareLoginAction.execute(context));
        assertTrue(PasswordlessWebflowUtils.isMultifactorAuthenticationAllowed(context));
        assertTrue(PasswordlessWebflowUtils.isDelegatedAuthenticationAllowed(context));
    }
    
}
