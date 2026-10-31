package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessRequestParser;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
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
@TestPropertySource(properties = {
    "cas.authn.passwordless.accounts.groovy.location=classpath:PasswordlessAccount.groovy",
    "CasFeatureModule.AccountManagement.enabled=true"
})
class VerifyPasswordlessAccountAuthenticationActionTests extends BasePasswordlessAuthenticationActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_VERIFY_PASSWORDLESS_ACCOUNT_AUTHN)
    private Action verifyPasswordlessAccountAuthenticationAction;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_PASSWORDLESS_CONTACT_METHODS)
    private Action passwordlessAccountProfileContactMethodsAction;

    @Test
    void verifyAccountProfileContactMethods() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication("needs-password"), context);
        assertNull(passwordlessAccountProfileContactMethodsAction.execute(context));
        assertEquals(List.of(Map.of("type", "email", "value", "casuser@example.org"), Map.of("type", "phone", "value", "123-456-7890")),
            context.getFlowScope().get(PasswordlessAccountProfileContactMethodsAction.FLOW_SCOPE_ATTR_CONTACT_METHODS));

        val unknown = MockRequestContext.create(applicationContext);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication("unknown"), unknown);
        assertNull(passwordlessAccountProfileContactMethodsAction.execute(unknown));
        assertFalse(unknown.getFlowScope().contains(PasswordlessAccountProfileContactMethodsAction.FLOW_SCOPE_ATTR_CONTACT_METHODS));
    }

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
