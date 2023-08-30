package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.config.CasWebflowAccountProfileConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockFlowExecutionContext;
import org.springframework.webflow.test.MockFlowSession;
import org.springframework.webflow.test.MockParameterMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AccountProfileUpdateSecurityQuestionsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("WebflowAccountActions")
@TestPropertySource(properties = {
    "cas.authn.pm.groovy.location=classpath:PasswordManagementService.groovy",
    "cas.authn.pm.core.enabled=true",
    "cas.authn.pm.reset.security-questions-enabled=true",
    "CasFeatureModule.AccountManagement.enabled=true"
})
@Import(CasWebflowAccountProfileConfiguration.class)
class AccountProfileUpdateSecurityQuestionsActionTests extends BasePasswordManagementActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_UPDATE_SECURITY_QUESTIONS)
    private Action accountProfileUpdateSecurityQuestionsAction;

    private MockParameterMap parameterMap;

    private RequestContext context;

    private static RequestContext getRequestContext(final MockParameterMap parameterMap) {
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        val context = mock(RequestContext.class);
        when(context.getFlashScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getFlowScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getRequestScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getMessageContext()).thenReturn(mock(MessageContext.class));
        when(context.getRequestParameters()).thenReturn(parameterMap);
        when(context.getExternalContext()).thenReturn(new ServletExternalContext(new MockServletContext(), request, response));
        when(context.getFlowExecutionContext()).thenReturn(
            new MockFlowExecutionContext(new MockFlowSession(new Flow("mockFlow"))));
        return context;
    }

    @BeforeEach
    public void setup() throws Exception {
        parameterMap = new MockParameterMap();
        context = getRequestContext(parameterMap);
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        val tgt = new MockTicketGrantingTicket("casuser");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        WebUtils.putTicketGrantingTicket(context, tgt);
        ticketRegistry.addTicket(tgt);
    }

    @Test
    void verifyMismatchedQuestionsAndAnswers() throws Throwable {
        parameterMap.put("questions", new String[]{"question1", "question2", "question3"});
        parameterMap.put("answers", new String[]{"answer1", "answer2"});
        val result = accountProfileUpdateSecurityQuestionsAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());
    }

    @Test
    void verifyShortQuestions() throws Throwable {
        parameterMap.put("questions", new String[]{"qaz", "zsxa", "123"});
        parameterMap.put("answers", new String[]{"a", "b", "c"});
        val result = accountProfileUpdateSecurityQuestionsAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());
    }

    @Test
    void verifyOperationPasses() throws Throwable {
        parameterMap.put("questions", new String[]{"question1", "question2"});
        parameterMap.put("answers", new String[]{"answer1", "answer2"});
        val result = accountProfileUpdateSecurityQuestionsAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
    }
}
