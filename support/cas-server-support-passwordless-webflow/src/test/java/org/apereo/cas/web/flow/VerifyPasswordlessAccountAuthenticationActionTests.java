package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.web.support.WebUtils;

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
public class VerifyPasswordlessAccountAuthenticationActionTests extends BasePasswordlessAuthenticationActionTests {
    @Autowired
    @Qualifier("verifyPasswordlessAccountAuthenticationAction")
    private Action verifyPasswordlessAccountAuthenticationAction;

    @Test
    public void verifyAction() throws Exception {
        val context = getRequestContext("casuser");
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, verifyPasswordlessAccountAuthenticationAction.execute(context).getId());
        val account = WebUtils.getPasswordlessAuthenticationAccount(context, PasswordlessUserAccount.class);
        assertNotNull(account);
    }

    @Test
    public void verifyNoUserInfoAction() throws Exception {
        val context = getRequestContext("nouserinfo");
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, verifyPasswordlessAccountAuthenticationAction.execute(context).getId());
        val account = WebUtils.getPasswordlessAuthenticationAccount(context, PasswordlessUserAccount.class);
        assertNull(account);
    }

    @Test
    public void verifyInvalidUser() throws Exception {
        val context = getRequestContext("unknown");
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, verifyPasswordlessAccountAuthenticationAction.execute(context).getId());
    }

    @Test
    public void verifyRequestPassword() throws Exception {
        val context = getRequestContext("needs-password");
        assertEquals(CasWebflowConstants.TRANSITION_ID_PROMPT, verifyPasswordlessAccountAuthenticationAction.execute(context).getId());
    }

    private static RequestContext getRequestContext(final String username) {
        val request = new MockHttpServletRequest();
        val exec = new MockFlowExecutionContext(new MockFlowSession(new Flow(CasWebflowConfigurer.FLOW_ID_LOGIN)));
        val context = mock(RequestContext.class);
        when(context.getRequestParameters()).thenReturn(
            new MockParameterMap().put("username", username));
        when(context.getMessageContext()).thenReturn(mock(MessageContext.class));
        when(context.getFlowScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getConversationScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getFlowExecutionContext()).thenReturn(exec);
        when(context.getExternalContext()).thenReturn(
            new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        return context;
    }
}
