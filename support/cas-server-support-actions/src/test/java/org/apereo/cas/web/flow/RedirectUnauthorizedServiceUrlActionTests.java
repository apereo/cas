package org.apereo.cas.web.flow;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.login.RedirectUnauthorizedServiceUrlAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RedirectUnauthorizedServiceUrlActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowActions")
public class RedirectUnauthorizedServiceUrlActionTests {

    @Test
    public void verifyUrl() throws Exception {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        context.setCurrentEvent(new EventFactorySupport().success(this));
        WebUtils.putUnauthorizedRedirectUrlIntoFlowScope(context, new URI("https://github.com/apereo/cas"));
        val action = new RedirectUnauthorizedServiceUrlAction(mock(ServicesManager.class), appCtx);
        assertNull(action.doExecute(context));
        assertEquals("https://github.com/apereo/cas",
            WebUtils.getUnauthorizedRedirectUrlFromFlowScope(context).toASCIIString());
    }

    @Test
    public void verifyScript() throws Exception {
        val appCtx = new StaticApplicationContext();
        appCtx.refresh();

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        context.setCurrentEvent(new EventFactorySupport().success(this));
        WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService());
        WebUtils.putUnauthorizedRedirectUrlIntoFlowScope(context, new URI("classpath:UnauthorizedServiceUrl.groovy"));
        val action = new RedirectUnauthorizedServiceUrlAction(mock(ServicesManager.class), appCtx);
        assertNull(action.doExecute(context));
        assertEquals("https://apereo.org/cas",
            WebUtils.getUnauthorizedRedirectUrlFromFlowScope(context).toASCIIString());
    }

}
