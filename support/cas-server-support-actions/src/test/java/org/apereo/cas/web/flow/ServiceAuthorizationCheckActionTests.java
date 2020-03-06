package org.apereo.cas.web.flow;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ServiceAuthorizationCheckActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Webflow")
public class ServiceAuthorizationCheckActionTests extends AbstractWebflowActionsTests {
    @Autowired
    @Qualifier("serviceAuthorizationCheck")
    private ObjectProvider<Action> action;

    @Test
    public void verifyNoServiceFound() {
        val request = new MockHttpServletRequest();
        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService("invalid-service-123"));
        assertThrows(UnauthorizedServiceException.class, () -> this.action.getObject().execute(context));
    }

    @Test
    public void verifyDisabledServiceFound() {
        val request = new MockHttpServletRequest();
        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService("cas-access-disabled"));
        assertThrows(UnauthorizedServiceException.class, () -> this.action.getObject().execute(context));
        assertNotNull(WebUtils.getUnauthorizedRedirectUrlFromFlowScope(context));
    }

    @Test
    public void verifyExclusiveAuthnDelegationMode() {
        val request = new MockHttpServletRequest();
        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService("cas-access-delegation"));
        assertDoesNotThrow(() -> this.action.getObject().execute(context));
        assertFalse(WebUtils.isCasLoginFormViewable(context));
    }
}
