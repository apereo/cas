package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.logout.DefaultLogoutExecutionPlan;
import org.apereo.cas.logout.DefaultSingleLogoutMessageCreator;
import org.apereo.cas.logout.DefaultSingleLogoutRequestContext;
import org.apereo.cas.logout.LogoutExecutionPlan;
import org.apereo.cas.logout.LogoutRequestStatus;
import org.apereo.cas.logout.SingleLogoutExecutionRequest;
import org.apereo.cas.logout.slo.DefaultSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.logout.slo.DefaultSingleLogoutServiceMessageHandler;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceLogoutType;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.http.SimpleHttpClientFactoryBean;
import org.apereo.cas.web.SimpleUrlValidatorFactoryBean;
import org.apereo.cas.web.flow.logout.FrontChannelLogoutAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Jerome Leleu
 * @since 4.0.0
 */
@Tag("WebflowActions")
public class FrontChannelLogoutActionTests {

    private LogoutExecutionPlan logoutExecutionPlan;

    @BeforeEach
    public void onSetUp() {
        val validator = new SimpleUrlValidatorFactoryBean(false).getObject();

        val servicesManager = mock(ServicesManager.class);
        when(servicesManager.findServiceBy(any(Service.class))).thenReturn(RegisteredServiceTestUtils.getRegisteredService());

        val handler = new DefaultSingleLogoutServiceMessageHandler(new SimpleHttpClientFactoryBean().getObject(),
            new DefaultSingleLogoutMessageCreator(), servicesManager,
            new DefaultSingleLogoutServiceLogoutUrlBuilder(servicesManager, validator), false,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()));

        this.logoutExecutionPlan = new DefaultLogoutExecutionPlan();
        logoutExecutionPlan.registerSingleLogoutServiceMessageHandler(handler);
    }

    @Test
    public void verifyNoRequests() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        WebUtils.putLogoutRequests(context, new ArrayList<>(0));
        val frontChannelLogoutAction = new FrontChannelLogoutAction(logoutExecutionPlan, false);
        val event = frontChannelLogoutAction.doExecute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, event.getId());
    }

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
        val frontChannelLogoutAction = new FrontChannelLogoutAction(logoutExecutionPlan, true);
        val event = frontChannelLogoutAction.doExecute(context);
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
        val frontChannelLogoutAction = new FrontChannelLogoutAction(logoutExecutionPlan, false);
        val event = frontChannelLogoutAction.doExecute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_PROPAGATE, event.getId());
    }
}
