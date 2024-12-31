package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessRequestParser;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.util.MockRequestContext;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link VerifyPasswordlessAccountAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import(BaseWebflowConfigurerTests.SharedTestConfiguration.class)
@Tag("WebflowAuthenticationActions")
@TestPropertySource(properties = "cas.authn.passwordless.accounts.groovy.location=classpath:PasswordlessAccount.groovy")
class VerifyPasswordlessAccountAuthenticationActionTests extends BasePasswordlessAuthenticationActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_VERIFY_PASSWORDLESS_ACCOUNT_AUTHN)
    private Action verifyPasswordlessAccountAuthenticationAction;

    @Test
    void verifyAction() throws Throwable {
        val context = getRequestContext("casuser");
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, verifyPasswordlessAccountAuthenticationAction.execute(context).getId());
        val account = PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(context, PasswordlessUserAccount.class);
        assertNotNull(account);
        assertNotNull(PasswordlessWebflowUtils.getPasswordlessAuthenticationRequest(context, PasswordlessAuthenticationRequest.class));
    }

    @Test
    void verifyNoUserInfoAction() throws Throwable {
        val context = getRequestContext("nouserinfo");
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, verifyPasswordlessAccountAuthenticationAction.execute(context).getId());
        val account = PasswordlessWebflowUtils.getPasswordlessAuthenticationAccount(context, PasswordlessUserAccount.class);
        assertNotNull(account);
    }

    @Test
    void verifyInvalidUser() throws Throwable {
        val context = getRequestContext("unknown");
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, verifyPasswordlessAccountAuthenticationAction.execute(context).getId());
    }

    @Test
    void verifyRequestPassword() throws Throwable {
        val context = getRequestContext("needs-password");
        assertEquals(CasWebflowConstants.TRANSITION_ID_PROMPT,
            verifyPasswordlessAccountAuthenticationAction.execute(context).getId());
    }

    @Test
    void verifySelectionMenu() throws Throwable {
        val context = getRequestContext("needs-selection");
        assertEquals(CasWebflowConstants.TRANSITION_ID_SELECT,
            verifyPasswordlessAccountAuthenticationAction.execute(context).getId());
    }

    @Test
    void verifyRequestPasswordForUserWithoutEmailOrPhone() throws Throwable {
        val context = getRequestContext("needs-password-user-without-email-or-phone");
        assertEquals(CasWebflowConstants.TRANSITION_ID_PROMPT, verifyPasswordlessAccountAuthenticationAction.execute(context).getId());
    }

    private RequestContext getRequestContext(final String username) throws Exception {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter(PasswordlessRequestParser.PARAMETER_USERNAME, username);
        return context;
    }
}
