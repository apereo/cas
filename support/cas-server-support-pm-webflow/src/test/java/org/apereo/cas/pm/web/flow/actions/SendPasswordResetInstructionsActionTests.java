package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SendPasswordResetInstructionsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnabledIfPortOpen(port = 25000)
@Tag("Mail")
public class SendPasswordResetInstructionsActionTests {

    @TestConfiguration("PasswordManagementTestConfiguration")
    public static class PasswordManagementTestConfiguration {
        @Bean
        @Autowired
        public PasswordManagementService passwordChangeService() {
            val service = mock(PasswordManagementService.class);
            when(service.createToken(any())).thenReturn(null);
            when(service.findUsername(any())).thenReturn("casuser");
            when(service.findEmail(any())).thenReturn("casuser@example.org");
            return service;
        }
    }

    @SuppressWarnings("ClassCanBeStatic")
    @Nested
    public class DefaultTests extends BasePasswordManagementActionTests {

        @BeforeEach
        public void setup() {
            val request = new MockHttpServletRequest();
            request.setRemoteAddr("223.456.789.000");
            request.setLocalAddr("123.456.789.000");
            request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
            ClientInfoHolder.setClientInfo(new ClientInfo(request));
        }

        @Test
        public void verifyAction() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            request.addParameter("username", "casuser");
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, sendPasswordResetInstructionsAction.execute(context).getId());
        }


        @Test
        public void verifyNoPhoneOrEmail() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            request.addParameter("username", "none");
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, sendPasswordResetInstructionsAction.execute(context).getId());
        }

        @Test
        public void verifyNoUsername() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, sendPasswordResetInstructionsAction.execute(context).getId());
        }
    }

    @SuppressWarnings("ClassCanBeStatic")
    @Nested
    @Import(PasswordManagementTestConfiguration.class)
    public class WithoutTokens extends BasePasswordManagementActionTests {

        @Test
        public void verifyNoLinkAction() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            request.addParameter("username", "unknown");
            WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, sendPasswordResetInstructionsAction.execute(context).getId());
        }
    }
}
