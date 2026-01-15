package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AcceptPasswordlessSelectionMenuActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("WebflowAuthenticationActions")
class AcceptPasswordlessSelectionMenuActionTests extends BasePasswordlessAuthenticationActionTests {

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_PASSWORDLESS_ACCEPT_SELECTION_MENU)
    private Action acceptAction;

    @Test
    void verifySelectionNotAllowed() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val account = PasswordlessUserAccount.builder()
            .username("casuser")
            .name("casuser")
            .allowSelectionMenu(false)
            .build();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, acceptAction.execute(context).getId());
    }

    @Test
    void verifyDelegation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter("selection", AcceptPasswordlessSelectionMenuAction.PasswordlessSelectionMenu.DELEGATION.name());
        val account = PasswordlessUserAccount.builder()
            .username("casuser")
            .name("casuser")
            .allowSelectionMenu(true)
            .delegatedAuthenticationEligible(TriStateBoolean.FALSE)
            .build();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, acceptAction.execute(context).getId());

        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account.withDelegatedAuthenticationEligible(TriStateBoolean.TRUE));
        assertEquals(CasWebflowConstants.TRANSITION_ID_DELEGATED_AUTHENTICATION_REDIRECT, acceptAction.execute(context).getId());
    }

    @Test
    void verifyPassword() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter("selection", AcceptPasswordlessSelectionMenuAction.PasswordlessSelectionMenu.PASSWORD.name());
        val account = PasswordlessUserAccount.builder()
            .username("casuser")
            .name("casuser")
            .allowSelectionMenu(true)
            .requestPassword(false)
            .build();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, acceptAction.execute(context).getId());

        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account.withRequestPassword(true));
        assertEquals(CasWebflowConstants.TRANSITION_ID_PROMPT, acceptAction.execute(context).getId());
        assertTrue(WebUtils.isCasLoginFormViewable(context));
    }

    @Test
    void verifyMultifactor() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter("selection", AcceptPasswordlessSelectionMenuAction.PasswordlessSelectionMenu.MFA.name());
        val account = PasswordlessUserAccount.builder()
            .username("casuser")
            .name("casuser")
            .allowSelectionMenu(true)
            .multifactorAuthenticationEligible(TriStateBoolean.FALSE)
            .build();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, acceptAction.execute(context).getId());

        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account.withMultifactorAuthenticationEligible(TriStateBoolean.TRUE));
        assertEquals(CasWebflowConstants.TRANSITION_ID_MFA, acceptAction.execute(context).getId());
    }

    @Test
    void verifyPasswordlessToken() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter("selection", AcceptPasswordlessSelectionMenuAction.PasswordlessSelectionMenu.TOKEN.name());
        val account = PasswordlessUserAccount.builder()
            .username("casuser")
            .name("casuser")
            .allowSelectionMenu(true)
            .build();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
        assertEquals(CasWebflowConstants.TRANSITION_ID_DISPLAY, acceptAction.execute(context).getId());
    }
}
