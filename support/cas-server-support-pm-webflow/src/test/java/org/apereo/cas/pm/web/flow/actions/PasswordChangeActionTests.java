package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowConfigurer;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This is {@link PasswordChangeActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowActions")
public class PasswordChangeActionTests {

    @SuppressWarnings("ClassCanBeStatic")
    @Nested
    @TestPropertySource(properties = "cas.authn.pm.core.password-policy-pattern=P@ss.+")
    public class DefaultTests extends BasePasswordManagementActionTests {

        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_PASSWORD_CHANGE)
        private Action passwordChangeAction;

        @Test
        public void verifyFailNoCreds() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());
            val changeReq = new PasswordChangeRequest();
            changeReq.setUsername("casuser");
            changeReq.setPassword("123456");
            context.getFlowScope().put(PasswordManagementWebflowConfigurer.FLOW_VAR_ID_PASSWORD, changeReq);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, passwordChangeAction.execute(context).getId());
        }

        @Test
        public void verifyFailsValidation() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            WebUtils.putCredential(context, RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword("casuser"));

            val changeReq = new PasswordChangeRequest();
            changeReq.setUsername("casuser");
            changeReq.setPassword("123456");
            context.getFlowScope().put(PasswordManagementWebflowConfigurer.FLOW_VAR_ID_PASSWORD, changeReq);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, passwordChangeAction.execute(context).getId());
        }

        @Test
        public void verifyChange() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            val token = UUID.randomUUID().toString();
            request.addParameter(PasswordManagementService.PARAMETER_PASSWORD_RESET_TOKEN, token);
            val tst = mock(TransientSessionTicket.class);
            when(tst.getId()).thenReturn(token);
            ticketRegistry.addTicket(tst);
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            WebUtils.putCredential(context,
                    RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Th!$isT3$t"));

            val changeReq = new PasswordChangeRequest();
            changeReq.setUsername("casuser");
            changeReq.setPassword("P@ssword");
            changeReq.setConfirmedPassword("P@ssword");
            context.getFlowScope().put(PasswordManagementWebflowConfigurer.FLOW_VAR_ID_PASSWORD, changeReq);
            assertEquals(CasWebflowConstants.TRANSITION_ID_PASSWORD_UPDATE_SUCCESS, passwordChangeAction.execute(context).getId());
            assertNotNull(ticketRegistry.getTicket(token));
        }

        @Test
        public void verifyChangeFails() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            WebUtils.putCredential(context,
                    RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("bad-credential", "P@ssword"));

            val changeReq = new PasswordChangeRequest();
            changeReq.setUsername("bad-credential");
            changeReq.setPassword("P@ssword");
            changeReq.setConfirmedPassword("P@ssword");
            context.getFlowScope().put(PasswordManagementWebflowConfigurer.FLOW_VAR_ID_PASSWORD, changeReq);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, passwordChangeAction.execute(context).getId());
        }

        @Test
        public void verifyPasswordRejected() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            WebUtils.putCredential(context,
                    RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("error-credential", "P@ssword"));

            val changeReq = new PasswordChangeRequest();
            changeReq.setUsername("error-credential");
            changeReq.setPassword("P@ssword");
            changeReq.setConfirmedPassword("P@ssword");
            context.getFlowScope().put(PasswordManagementWebflowConfigurer.FLOW_VAR_ID_PASSWORD, changeReq);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, passwordChangeAction.execute(context).getId());
        }
    }

    @SuppressWarnings("ClassCanBeStatic")
    @Nested
    @TestPropertySource(properties = {
            "cas.authn.pm.core.password-policy-pattern=P@ss.+",
            "cas.authn.pm.reset.remove-token-after-successful-change=true"
    })
    public class RemoveTokenTests extends BasePasswordManagementActionTests {

        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_PASSWORD_CHANGE)
        private Action passwordChangeAction;

        @Test
        public void verifyChange() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            val token = UUID.randomUUID().toString();
            request.addParameter(PasswordManagementService.PARAMETER_PASSWORD_RESET_TOKEN, token);
            val tst = mock(TransientSessionTicket.class);
            when(tst.getId()).thenReturn(token);
            ticketRegistry.addTicket(tst);
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            WebUtils.putCredential(context,
                    RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Th!$isT3$t"));

            val changeReq = new PasswordChangeRequest();
            changeReq.setUsername("casuser");
            changeReq.setPassword("P@ssword");
            changeReq.setConfirmedPassword("P@ssword");
            context.getFlowScope().put(PasswordManagementWebflowConfigurer.FLOW_VAR_ID_PASSWORD, changeReq);
            assertEquals(CasWebflowConstants.TRANSITION_ID_PASSWORD_UPDATE_SUCCESS, passwordChangeAction.execute(context).getId());
            assertNull(ticketRegistry.getTicket(token));
        }
    }
}
