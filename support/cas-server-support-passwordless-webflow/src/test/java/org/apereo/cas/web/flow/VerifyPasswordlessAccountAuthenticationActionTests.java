package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessRequestParser;
import org.apereo.cas.api.PasswordlessUserAccount;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.message.MessageContext;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockFlowExecutionContext;
import org.springframework.webflow.test.MockFlowSession;
import org.springframework.webflow.test.MockParameterMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    void verifyRequestPasswordForUserWithoutEmailOrPhone() throws Throwable {
        val context = getRequestContext("needs-password-user-without-email-or-phone");
        assertEquals(CasWebflowConstants.TRANSITION_ID_PROMPT, verifyPasswordlessAccountAuthenticationAction.execute(context).getId());
    }

    private static RequestContext getRequestContext(final String username) {
        val request = new MockHttpServletRequest();
        val exec = new MockFlowExecutionContext(new MockFlowSession(new Flow(CasWebflowConfigurer.FLOW_ID_LOGIN)));
        val context = mock(RequestContext.class);
        when(context.getRequestParameters()).thenReturn(
            new MockParameterMap().put(PasswordlessRequestParser.PARAMETER_USERNAME, username));
        when(context.getMessageContext()).thenReturn(mock(MessageContext.class));
        when(context.getFlowScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getConversationScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getFlowExecutionContext()).thenReturn(exec);
        when(context.getExternalContext()).thenReturn(
            new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        return context;
    }
}
