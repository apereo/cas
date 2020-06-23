package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.message.MessageContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SendForgotUsernameInstructionsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@EnabledIfPortOpen(port = 25000)
@Tag("Mail")
public class SendForgotUsernameInstructionsActionTests extends BasePasswordManagementActionTests {
    @Autowired
    @Qualifier("sendForgotUsernameInstructionsAction")
    private Action sendForgotUsernameInstructionsAction;

    @Test
    public void verifyNoEmailOrUser() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val context = mock(RequestContext.class);
        when(context.getMessageContext()).thenReturn(mock(MessageContext.class));
        when(context.getFlowScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getExternalContext()).thenReturn(new ServletExternalContext(new MockServletContext(), request, response));

        var result = sendForgotUsernameInstructionsAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());

        request.addParameter("email", "123456");
        result = sendForgotUsernameInstructionsAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());

        request.setParameter("email", "casuser@baddomain.org");
        result = sendForgotUsernameInstructionsAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());
    }

    @Test
    public void verifyOp() throws Exception {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val context = mock(RequestContext.class);
        when(context.getMessageContext()).thenReturn(mock(MessageContext.class));
        when(context.getFlowScope()).thenReturn(new LocalAttributeMap<>());
        when(context.getExternalContext()).thenReturn(new ServletExternalContext(new MockServletContext(), request, response));

        request.setParameter("email", "casuser@apereo.org");
        var result = sendForgotUsernameInstructionsAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
    }
}
