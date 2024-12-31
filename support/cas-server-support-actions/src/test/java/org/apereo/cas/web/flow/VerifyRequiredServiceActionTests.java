package org.apereo.cas.web.flow;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.login.VerifyRequiredServiceAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;
import jakarta.servlet.http.Cookie;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link VerifyRequiredServiceActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@TestPropertySource(properties = {
    "cas.sso.services.required-service-pattern=^https://www.google.com.*",
    "cas.tgc.crypto.enabled=false"
})
@Tag("WebflowServiceActions")
class VerifyRequiredServiceActionTests extends AbstractWebflowActionsTests {

    private Action verifyRequiredServiceAction;
    private MockRequestContext requestContext;
    private TicketRegistrySupport ticketRegistrySupport;

    @BeforeEach
    void onSetUp() throws Throwable {
        ticketRegistrySupport = mock(TicketRegistrySupport.class);
        verifyRequiredServiceAction = new VerifyRequiredServiceAction(getServicesManager(),
            getTicketGrantingTicketCookieGenerator(), casProperties, ticketRegistrySupport);
        this.requestContext = MockRequestContext.create(applicationContext);
    }

    @Test
    void verifySkipCheckNoService() throws Throwable {
        val result = verifyRequiredServiceAction.execute(this.requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
    }

    @Test
    void verifySkipServiceMatchesPattern() throws Throwable {
        WebUtils.putServiceIntoFlowScope(this.requestContext, RegisteredServiceTestUtils.getService("https://www.google.com/example"));
        val result = verifyRequiredServiceAction.execute(this.requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
    }

    @Test
    void verifySkipServiceByProperty() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService("^https://yahoo.com.+");
        service.setEvaluationOrder(1);
        service.setProperties(CollectionUtils.wrap(
            RegisteredServiceProperty.RegisteredServiceProperties.SKIP_REQUIRED_SERVICE_CHECK.getPropertyName(),
            new DefaultRegisteredServiceProperty("true")));
        getServicesManager().save(service);
        WebUtils.putServiceIntoFlowScope(this.requestContext, RegisteredServiceTestUtils.getService("https://yahoo.com/"));
        val result = verifyRequiredServiceAction.execute(this.requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
    }

    @Test
    void verifySkipNoSsoServices() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService("^https://app1.com.+");
        getServicesManager().save(service);
        WebUtils.putServiceIntoFlowScope(this.requestContext, RegisteredServiceTestUtils.getService("https://app1.com/"));

        val tgt = new MockTicketGrantingTicket("casuser");
        when(ticketRegistrySupport.getTicketGrantingTicket(anyString())).thenReturn(tgt);
        requestContext.setHttpRequestCookies(new Cookie(casProperties.getTgc().getName(), tgt.getId()));

        val result = verifyRequiredServiceAction.execute(this.requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
    }

    @Test
    void verifySkipWithSsoServicesMismatch() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService("^https://app2.com.+");
        getServicesManager().save(service);
        WebUtils.putServiceIntoFlowScope(this.requestContext, RegisteredServiceTestUtils.getService("https://app2.com/"));

        val tgt = new MockTicketGrantingTicket("casuser");
        tgt.grantServiceTicket(RegisteredServiceTestUtils.getService("https://google.com/"),
            serviceTicketSessionTrackingPolicy);

        when(ticketRegistrySupport.getTicketGrantingTicket(anyString())).thenReturn(tgt);
        requestContext.setHttpRequestCookies(new Cookie(casProperties.getTgc().getName(), tgt.getId()));

        assertThrows(NoSuchFlowExecutionException.class, () -> verifyRequiredServiceAction.execute(this.requestContext));
    }

    @Test
    void verifySkipWithSsoServicesMatch() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService("^https://app2.com.+");
        getServicesManager().save(service);
        WebUtils.putServiceIntoFlowScope(this.requestContext, RegisteredServiceTestUtils.getService("https://app2.com/"));

        val tgt = new MockTicketGrantingTicket("casuser");
        tgt.grantServiceTicket(RegisteredServiceTestUtils.getService("https://www.google.com/"),
            serviceTicketSessionTrackingPolicy);

        when(ticketRegistrySupport.getTicketGrantingTicket(anyString())).thenReturn(tgt);
        requestContext.setHttpRequestCookies(new Cookie(casProperties.getTgc().getName(), tgt.getId()));

        assertDoesNotThrow(() -> verifyRequiredServiceAction.execute(this.requestContext));
    }
}
