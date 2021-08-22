package org.apereo.cas.web.flow;

import org.apereo.cas.logout.DefaultSingleLogoutRequestContext;
import org.apereo.cas.logout.LogoutRequestStatus;
import org.apereo.cas.logout.SingleLogoutExecutionRequest;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceLogoutType;
import org.apereo.cas.services.RegisteredServiceTestUtils;
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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jerome Leleu
 * @since 4.0.0
 */
@Tag("WebflowActions")
public class FrontChannelLogoutActionTests {
    
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = "cas.slo.disabled=true")
    public class SingleLogoutDisabledTests extends AbstractWebflowActionsTests {

        @Autowired
        @Qualifier("frontChannelLogoutAction")
        private Action frontChannelLogoutAction;

        @Test
        public void verifyNoSlo() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            val id = UUID.randomUUID().toString();
            val sloReq = DefaultSingleLogoutRequestContext.builder()
                .registeredService(RegisteredServiceTestUtils.getRegisteredService())
                .service(RegisteredServiceTestUtils.getService())
                .executionRequest(SingleLogoutExecutionRequest.builder()
                    .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
                    .build())
                .logoutType(RegisteredServiceLogoutType.BACK_CHANNEL)
                .ticketId(id)
                .status(LogoutRequestStatus.NOT_ATTEMPTED)
                .logoutUrl(new URL("https://apereo.org/cas"))
                .build();

            WebUtils.putLogoutRequests(context, List.of(sloReq));
            val event = frontChannelLogoutAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = "cas.slo.disabled=false")
    public class SingleLogoutEnabledTests extends AbstractWebflowActionsTests {

        @Autowired
        @Qualifier("frontChannelLogoutAction")
        private Action frontChannelLogoutAction;

        @Test
        public void verifyLogoutNone() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            val id = UUID.randomUUID().toString();
            val sloReq = DefaultSingleLogoutRequestContext.builder()
                .registeredService(RegisteredServiceTestUtils.getRegisteredService())
                .service(RegisteredServiceTestUtils.getService())
                .executionRequest(SingleLogoutExecutionRequest.builder()
                    .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
                    .build())
                .logoutType(RegisteredServiceLogoutType.NONE)
                .ticketId(id)
                .logoutUrl(new URL("https://apereo.org/cas"))
                .status(LogoutRequestStatus.SUCCESS)
                .build();

            WebUtils.putLogoutRequests(context, List.of(sloReq));
            val event = frontChannelLogoutAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
        }

        @Test
        public void verifyLogout() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            val id = UUID.randomUUID().toString();
            val sloReq = DefaultSingleLogoutRequestContext.builder()
                .registeredService(RegisteredServiceTestUtils.getRegisteredService())
                .service(RegisteredServiceTestUtils.getService())
                .executionRequest(SingleLogoutExecutionRequest.builder()
                    .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
                    .build())
                .logoutType(RegisteredServiceLogoutType.BACK_CHANNEL)
                .ticketId(id)
                .logoutUrl(new URL("https://apereo.org/cas"))
                .status(LogoutRequestStatus.NOT_ATTEMPTED)
                .build();

            WebUtils.putLogoutRequests(context, List.of(sloReq));
            val event = frontChannelLogoutAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_PROPAGATE, event.getId());
        }
        
        @Test
        public void verifyNoRequests() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            RequestContextHolder.setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());

            WebUtils.putLogoutRequests(context, new ArrayList<>(0));
            val event = frontChannelLogoutAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
        }
    }

}
