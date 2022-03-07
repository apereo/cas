package org.apereo.cas.pm.web.flow.actions;

import lombok.val;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.message.MessageContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockParameterMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This is {@link SendForgotUsernameInstructionsActionEmailMessageBodyTextTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@EnabledIfPortOpen(port = 25000)
public class SendForgotUsernameInstructionsActionEmailMessageBodyTextTests {

    @Tag("Mail")
    @Nested
    @TestPropertySource(properties = "cas.authn.pm.forgot-username.mail.text=classpath:ForgotUsernameEmailBody.groovy")
    @Import(SendForgotUsernameInstructionsActionEmailMessageBodyTextTests.Config.class)
    @SuppressWarnings("ClassCanBeStatic")
    public class DefaultTests extends BasePasswordManagementActionTests {

        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_SEND_FORGOT_USERNAME_INSTRUCTIONS_ACTION)
        protected Action sendForgotUsernameInstructionsAction;

        @Autowired
        @Qualifier(CommunicationsManager.BEAN_NAME)
        private CommunicationsManager communicationsManager;

        @Captor
        ArgumentCaptor<String> bodyCaptor;

        @Test
        public void verifyBodyContainsUsername() throws Exception {
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();

            val context = mock(RequestContext.class);
            when(context.getMessageContext()).thenReturn(mock(MessageContext.class));
            var flash = new LocalAttributeMap<Object>();
            when(context.getFlashScope()).thenReturn(flash);
            var flow = new LocalAttributeMap<Object>();
            when(context.getFlowScope()).thenReturn(flow);
            var req = new LocalAttributeMap<Object>();
            when(context.getRequestScope()).thenReturn(req);
            var conv = new LocalAttributeMap<Object>();
            when(context.getConversationScope()).thenReturn(conv);
            when(context.getRequestParameters()).thenReturn(new MockParameterMap());
            when(context.getExternalContext()).thenReturn(new ServletExternalContext(new MockServletContext(), request, response));

            when(communicationsManager.isMailSenderDefined()).thenReturn(true);
            when(communicationsManager.validate()).thenReturn(true);
            when(communicationsManager.email(any(), any(), any())).thenReturn(true);

            request.setParameter("username", "casuser");
            request.setParameter("email", "casuser@apereo.org");
            sendForgotUsernameInstructionsAction.execute(context);

            @SuppressWarnings("unused") var httpServletResponseFromExternalWebflowContext = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);

            verify(communicationsManager).email(any(), any(), bodyCaptor.capture());

            assertEquals("Hello uid with email null, your affiliation is developer", bodyCaptor.getValue());
        }
    }

    @Tag("Mail")
    @Nested
    @TestPropertySource(properties = "spring.boot.config.CasPersonDirectoryTestConfiguration.enabled=false")
    @Import(SendForgotUsernameInstructionsActionEmailMessageBodyTextTests.Config.class)
    @SuppressWarnings("ClassCanBeStatic")
    public class NoPrincipalResolutionTests extends BasePasswordManagementActionTests {

        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_SEND_FORGOT_USERNAME_INSTRUCTIONS_ACTION)
        protected Action sendForgotUsernameInstructionsAction;

        @Autowired
        @Qualifier(CommunicationsManager.BEAN_NAME)
        private CommunicationsManager communicationsManager;

        @Captor
        ArgumentCaptor<String> bodyCaptor;

        @Test
        public void verifyBodyContainsUsername() throws Exception {
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();

            val context = mock(RequestContext.class);
            when(context.getMessageContext()).thenReturn(mock(MessageContext.class));
            var flash = new LocalAttributeMap<Object>();
            when(context.getFlashScope()).thenReturn(flash);
            var flow = new LocalAttributeMap<Object>();
            when(context.getFlowScope()).thenReturn(flow);
            var req = new LocalAttributeMap<Object>();
            when(context.getRequestScope()).thenReturn(req);
            var conv = new LocalAttributeMap<Object>();
            when(context.getConversationScope()).thenReturn(conv);
            when(context.getRequestParameters()).thenReturn(new MockParameterMap());
            when(context.getExternalContext()).thenReturn(new ServletExternalContext(new MockServletContext(), request, response));

            when(communicationsManager.isMailSenderDefined()).thenReturn(true);
            when(communicationsManager.validate()).thenReturn(true);
            when(communicationsManager.email(any(), any(), any())).thenReturn(true);

            request.setParameter("username", "casuser");
            request.setParameter("email", "casuser@apereo.org");
            sendForgotUsernameInstructionsAction.execute(context);

            @SuppressWarnings("unused") var httpServletResponseFromExternalWebflowContext = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);

            verify(communicationsManager).email(any(), any(), bodyCaptor.capture());

            assertEquals("Your current username is: casuser", bodyCaptor.getValue());
        }
    }

    @TestConfiguration
    public static class Config {
        @Bean
        public CommunicationsManager communicationsManager() {
            return mock(CommunicationsManager.class);
        }
    }
}
