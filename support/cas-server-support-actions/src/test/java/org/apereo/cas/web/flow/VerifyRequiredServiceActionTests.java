package org.apereo.cas.web.flow;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.login.VerifyRequiredServiceAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;
import org.springframework.webflow.test.MockRequestContext;

import javax.servlet.http.Cookie;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link VerifyRequiredServiceActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@TestPropertySource(properties = {
    "cas.sso.required-service-pattern=^https://www.google.com.*",
    "cas.tgc.crypto.enabled=false"
})
@Tag("Webflow")
public class VerifyRequiredServiceActionTests extends AbstractWebflowActionsTests {

    private Action verifyRequiredServiceAction;
    private MockRequestContext requestContext;
    private MockHttpServletRequest httpRequest;
    private TicketRegistrySupport ticketRegistrySupport;

    @BeforeEach
    public void onSetUp() {
        this.ticketRegistrySupport = mock(TicketRegistrySupport.class);

        this.verifyRequiredServiceAction = new VerifyRequiredServiceAction(getServicesManager(),
            getTicketGrantingTicketCookieGenerator(),
            casProperties, ticketRegistrySupport);

        val response = new MockHttpServletResponse();
        this.requestContext = new MockRequestContext();
        this.httpRequest = new MockHttpServletRequest();
        this.requestContext.setExternalContext(new ServletExternalContext(new MockServletContext(), this.httpRequest, response));
    }

    @Test
    public void verifySkipCheckNoService() throws Exception {
        val result = verifyRequiredServiceAction.execute(this.requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
    }

    @Test
    public void verifySkipServiceMatchesPattern() throws Exception {
        WebUtils.putServiceIntoFlowScope(this.requestContext, RegisteredServiceTestUtils.getService("https://www.google.com/example"));
        val result = verifyRequiredServiceAction.execute(this.requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
    }

    @Test
    public void verifySkipServiceByProperty() throws Exception {
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
    public void verifySkipNoSsoServices() throws Exception {
        val service = RegisteredServiceTestUtils.getRegisteredService("^https://app1.com.+");
        getServicesManager().save(service);
        WebUtils.putServiceIntoFlowScope(this.requestContext, RegisteredServiceTestUtils.getService("https://app1.com/"));

        val tgt = new MockTicketGrantingTicket("casuser");
        when(ticketRegistrySupport.getTicketGrantingTicket(anyString())).thenReturn(tgt);
        this.httpRequest.setCookies(new Cookie(casProperties.getTgc().getName(), tgt.getId()));

        val result = verifyRequiredServiceAction.execute(this.requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
    }

    @Test
    public void verifySkipWithSsoServicesMismatch() {
        val service = RegisteredServiceTestUtils.getRegisteredService("^https://app2.com.+");
        getServicesManager().save(service);
        WebUtils.putServiceIntoFlowScope(this.requestContext, RegisteredServiceTestUtils.getService("https://app2.com/"));

        val tgt = new MockTicketGrantingTicket("casuser");
        tgt.grantServiceTicket(RegisteredServiceTestUtils.getService("https://google.com/"));

        when(ticketRegistrySupport.getTicketGrantingTicket(anyString())).thenReturn(tgt);
        this.httpRequest.setCookies(new Cookie(casProperties.getTgc().getName(), tgt.getId()));

        assertThrows(NoSuchFlowExecutionException.class, () -> verifyRequiredServiceAction.execute(this.requestContext));
    }

    @Test
    public void verifySkipWithSsoServicesMatch() {
        val service = RegisteredServiceTestUtils.getRegisteredService("^https://app2.com.+");
        getServicesManager().save(service);
        WebUtils.putServiceIntoFlowScope(this.requestContext, RegisteredServiceTestUtils.getService("https://app2.com/"));

        val tgt = new MockTicketGrantingTicket("casuser");
        tgt.grantServiceTicket(RegisteredServiceTestUtils.getService("https://www.google.com/"));

        when(ticketRegistrySupport.getTicketGrantingTicket(anyString())).thenReturn(tgt);
        this.httpRequest.setCookies(new Cookie(casProperties.getTgc().getName(), tgt.getId()));

        assertDoesNotThrow(() -> verifyRequiredServiceAction.execute(this.requestContext));
    }
}
