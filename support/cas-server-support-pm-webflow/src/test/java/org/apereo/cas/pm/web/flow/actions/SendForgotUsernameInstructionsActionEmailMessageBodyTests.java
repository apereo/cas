package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.notifications.mail.EmailCommunicationResult;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
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
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockParameterMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SendForgotUsernameInstructionsActionEmailMessageBodyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@EnabledIfListeningOnPort(port = 25000)
@Tag("Mail")
class SendForgotUsernameInstructionsActionEmailMessageBodyTests extends BasePasswordManagementActionTests {

    private static RequestContext getRequestContext(final MockHttpServletRequest request,
                                                    final MockHttpServletResponse response) {
        val context = mock(RequestContext.class);
        when(context.getMessageContext()).thenReturn(mock(MessageContext.class));
        when(context.getFlashScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getFlowScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getRequestScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getConversationScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getRequestParameters()).thenReturn(new MockParameterMap());
        when(context.getExternalContext()).thenReturn(new ServletExternalContext(new MockServletContext(), request, response));
        return context;
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.pm.forgot-username.mail.text=classpath:ForgotUsernameEmailBody.groovy")
    @SuppressWarnings("ClassCanBeStatic")
    class DefaultTests extends BasePasswordManagementActionTests {

        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_SEND_FORGOT_USERNAME_INSTRUCTIONS_ACTION)
        protected Action sendForgotUsernameInstructionsAction;

        @Test
        public void verifyOp() throws Exception {
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            val context = getRequestContext(request, response);

            request.setParameter("email", "casuser@apereo.org");
            val result = sendForgotUsernameInstructionsAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
        }

        @Test
        public void verifyBodyContainsUsername() throws Exception {
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            val context = getRequestContext(request, response);

            request.setParameter("username", "casuser");
            request.setParameter("email", "casuser@apereo.org");
            val resultEvent = sendForgotUsernameInstructionsAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, resultEvent.getId());

            val resultAttributeName = new EventFactorySupport().getResultAttributeName();
            val emailResult = resultEvent.getAttributes().get(resultAttributeName, EmailCommunicationResult.class);
            assertTrue(emailResult.isSuccess());
            assertEquals("Hello uid with email casuser@apereo.org, your affiliation is developer", emailResult.getBody());
        }
    }

    @Nested
    @TestPropertySource(properties = "spring.boot.config.CasPersonDirectoryTestConfiguration.enabled=false")
    @SuppressWarnings("ClassCanBeStatic")
    class NoPrincipalResolutionTests extends BasePasswordManagementActionTests {

        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_SEND_FORGOT_USERNAME_INSTRUCTIONS_ACTION)
        protected Action sendForgotUsernameInstructionsAction;

        @Test
        public void verifyBodyContainsUsername() throws Exception {
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            val context = getRequestContext(request, response);

            request.setParameter("username", "casuser");
            request.setParameter("email", "casuser@apereo.org");
            val resultEvent = sendForgotUsernameInstructionsAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, resultEvent.getId());

            val resultAttributeName = new EventFactorySupport().getResultAttributeName();
            val emailResult = resultEvent.getAttributes().get(resultAttributeName, EmailCommunicationResult.class);
            assertTrue(emailResult.isSuccess());
            assertEquals("Your current username is: casuser", emailResult.getBody());
        }

    }
}
