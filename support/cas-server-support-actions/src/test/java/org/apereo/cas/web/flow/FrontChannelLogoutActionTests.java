package org.apereo.cas.web.flow;

import org.apereo.cas.logout.DefaultSingleLogoutRequestContext;
import org.apereo.cas.logout.LogoutRequestStatus;
import org.apereo.cas.logout.slo.SingleLogoutExecutionRequest;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceLogoutType;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jerome Leleu
 * @since 4.0.0
 */
@Tag("WebflowActions")
class FrontChannelLogoutActionTests {

    @Nested
    @TestPropertySource(properties = "cas.slo.disabled=true")
    class SingleLogoutDisabledTests extends AbstractWebflowActionsTests {

        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_FRONT_CHANNEL_LOGOUT)
        private Action frontChannelLogoutAction;

        @Test
        void verifyNoSlo() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
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
                .logoutUrl(new URI("https://apereo.org/cas").toURL())
                .build();

            WebUtils.putLogoutRequests(context, List.of(sloReq));
            val event = frontChannelLogoutAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.slo.disabled=false")
    class SingleLogoutEnabledTests extends AbstractWebflowActionsTests {

        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_FRONT_CHANNEL_LOGOUT)
        private Action frontChannelLogoutAction;

        @Test
        void verifyLogoutNone() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            val id = UUID.randomUUID().toString();
            val sloReq = DefaultSingleLogoutRequestContext.builder()
                .registeredService(RegisteredServiceTestUtils.getRegisteredService())
                .service(RegisteredServiceTestUtils.getService())
                .executionRequest(SingleLogoutExecutionRequest.builder()
                    .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
                    .build())
                .logoutType(RegisteredServiceLogoutType.NONE)
                .ticketId(id)
                .logoutUrl(new URI("https://apereo.org/cas").toURL())
                .status(LogoutRequestStatus.SUCCESS)
                .build();

            WebUtils.putLogoutRequests(context, List.of(sloReq));
            val event = frontChannelLogoutAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
        }

        @Test
        void verifyLogout() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            val id = UUID.randomUUID().toString();
            val sloReq = DefaultSingleLogoutRequestContext.builder()
                .registeredService(RegisteredServiceTestUtils.getRegisteredService())
                .service(RegisteredServiceTestUtils.getService())
                .executionRequest(SingleLogoutExecutionRequest.builder()
                    .ticketGrantingTicket(new MockTicketGrantingTicket("casuser"))
                    .build())
                .logoutType(RegisteredServiceLogoutType.BACK_CHANNEL)
                .ticketId(id)
                .logoutUrl(new URI("https://apereo.org/cas").toURL())
                .status(LogoutRequestStatus.NOT_ATTEMPTED)
                .build();

            WebUtils.putLogoutRequests(context, List.of(sloReq));
            val event = frontChannelLogoutAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_PROPAGATE, event.getId());
            assertTrue(context.getFlowScope().contains("logoutPropagationType"));
        }

        @Test
        void verifyNoRequests() throws Throwable {
            val context = MockRequestContext.create(applicationContext);


            WebUtils.putLogoutRequests(context, new ArrayList<>());
            val event = frontChannelLogoutAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
        }
    }

}
