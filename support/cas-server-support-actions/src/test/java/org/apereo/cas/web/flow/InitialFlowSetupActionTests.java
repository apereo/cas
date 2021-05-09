package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("WebflowActions")
@TestPropertySource(properties = {
    "cas.authn.policy.source-selection-enabled=true",
    "cas.sso.sso-enabled=false",
    "cas.tgc.crypto.enabled=false"
})
public class InitialFlowSetupActionTests extends AbstractWebflowActionsTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_INITIAL_FLOW_SETUP)
    private Action action;

    @Test
    public void verifyResponseStatusAsError() throws Exception {
        val context = new MockRequestContext();
        var response = new MockHttpServletResponse();
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        context.setExternalContext(new ServletExternalContext(new MockServletContext(),
            new MockHttpServletRequest(), response));
        assertThrows(UnauthorizedServiceException.class, () -> action.execute(context));
    }

    @Test
    public void verifyNoServiceFound() throws Exception {
        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(),
            new MockHttpServletRequest(), new MockHttpServletResponse()));
        val event = this.action.execute(context);
        assertNull(WebUtils.getService(context));
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
    }

    @Test
    public void verifyServiceFound() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "test");
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        val event = this.action.execute(context);

        assertEquals("test", WebUtils.getService(context).getId());
        assertNotNull(WebUtils.getRegisteredService(context));
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
    }

    @Test
    public void verifyServiceStrategy() throws Exception {
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();
        request.setMethod(HttpMethod.POST.name());

        val id = UUID.randomUUID().toString();
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(id);
        val accessStrategy = new DefaultRegisteredServiceAccessStrategy();
        accessStrategy.setUnauthorizedRedirectUrl(new URI("https://apereo.org/cas"));
        registeredService.setAccessStrategy(accessStrategy);
        request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, id);
        getServicesManager().save(registeredService);

        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val event = this.action.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
    }

    @Test
    public void verifyTgtNoSso() throws Exception {
        val response = new MockHttpServletResponse();
        val request = new MockHttpServletRequest();

        val tgt = new MockTicketGrantingTicket("casuser");
        getTicketRegistry().addTicket(tgt);
        getTicketGrantingTicketCookieGenerator().addCookie(response, tgt.getId());
        request.setCookies(response.getCookies());

        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val event = this.action.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        assertTrue(WebUtils.isExistingSingleSignOnSessionAvailable(context));
    }
}
