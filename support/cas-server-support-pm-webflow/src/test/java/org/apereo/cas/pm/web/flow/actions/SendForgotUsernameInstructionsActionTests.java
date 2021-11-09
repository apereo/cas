package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.message.MessageContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockParameterMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SendForgotUsernameInstructionsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@EnabledIfPortOpen(port = 25000)
public class SendForgotUsernameInstructionsActionTests {

    @Tag("Mail")
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class DefaultTests extends BasePasswordManagementActionTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_SEND_FORGOT_USERNAME_INSTRUCTIONS_ACTION)
        protected Action sendForgotUsernameInstructionsAction;

        @Test
        public void verifyNoEmailOrUser() throws Exception {
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();

            val context = mock(RequestContext.class);
            when(context.getMessageContext()).thenReturn(mock(MessageContext.class));
            when(context.getFlashScope()).thenReturn(new LocalAttributeMap<>());
            when(context.getFlowScope()).thenReturn(new LocalAttributeMap<>());
            when(context.getRequestScope()).thenReturn(new LocalAttributeMap<>());
            when(context.getConversationScope()).thenReturn(new LocalAttributeMap<>());
            when(context.getRequestParameters()).thenReturn(new MockParameterMap());
            when(context.getExternalContext()).thenReturn(new ServletExternalContext(new MockServletContext(), request, response));

            var result = sendForgotUsernameInstructionsAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());

            request.addParameter("email", "123456");
            result = sendForgotUsernameInstructionsAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());

            request.setParameter("email", "casuser@baddomain.org");
            result = sendForgotUsernameInstructionsAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());
            assertFalse(context.getFlashScope().contains(Principal.class.getName()));
        }

        @Test
        public void verifyOp() throws Exception {
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();

            val context = mock(RequestContext.class);
            when(context.getMessageContext()).thenReturn(mock(MessageContext.class));
            when(context.getFlowScope()).thenReturn(new LocalAttributeMap<>());
            when(context.getRequestScope()).thenReturn(new LocalAttributeMap<>());
            when(context.getConversationScope()).thenReturn(new LocalAttributeMap<>());
            when(context.getFlashScope()).thenReturn(new LocalAttributeMap<>());
            when(context.getRequestParameters()).thenReturn(new MockParameterMap());
            when(context.getExternalContext()).thenReturn(new ServletExternalContext(new MockServletContext(), request, response));

            request.setParameter("email", "casuser@apereo.org");
            var result = sendForgotUsernameInstructionsAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
            assertTrue(context.getFlashScope().contains(Principal.class.getName()));
        }
    }

    @Tag("Mail")
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = "spring.boot.config.CasPersonDirectoryTestConfiguration.enabled=false")
    public class NoPrincipalResolutionTests extends BasePasswordManagementActionTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_SEND_FORGOT_USERNAME_INSTRUCTIONS_ACTION)
        protected Action sendForgotUsernameInstructionsAction;

        @Test
        public void verifyOpWithoutPrincipalResolution() throws Exception {
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();

            val context = mock(RequestContext.class);
            when(context.getMessageContext()).thenReturn(mock(MessageContext.class));
            when(context.getFlowScope()).thenReturn(new LocalAttributeMap<>());
            when(context.getFlashScope()).thenReturn(new LocalAttributeMap<>());
            when(context.getRequestScope()).thenReturn(new LocalAttributeMap<>());
            when(context.getConversationScope()).thenReturn(new LocalAttributeMap<>());
            when(context.getRequestParameters()).thenReturn(new MockParameterMap());
            when(context.getExternalContext()).thenReturn(
                new ServletExternalContext(new MockServletContext(), request, response));

            request.setParameter("email", "casuser@apereo.org");
            var result = sendForgotUsernameInstructionsAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
            assertFalse(context.getFlashScope().contains(Principal.class.getName()));
        }
    }
}
