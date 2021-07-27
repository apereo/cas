package org.apereo.cas.logout;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.configuration.model.core.logout.LogoutProperties;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockServletContext;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultLogoutRedirectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Logout")
public class DefaultLogoutRedirectionStrategyTests {

    @Test
    public void verifyNoRedirectUrl() {
        val props = new LogoutProperties();
        props.setFollowServiceRedirects(true);

        val request = new MockHttpServletRequest();
        val context = getMockRequestContext(request);

        val logoutUrlBuilder = mock(SingleLogoutServiceLogoutUrlBuilder.class);
        when(logoutUrlBuilder.isServiceAuthorized(any(WebApplicationService.class), any())).thenReturn(Boolean.TRUE);

        val strategy = new DefaultLogoutRedirectionStrategy(mock(ServiceFactory.class), props, logoutUrlBuilder);

        assertTrue(strategy.supports(context));
        strategy.handle(context);
        assertNull(WebUtils.getLogoutRedirectUrl(context, String.class));

        String logoutRedirectUrl = "https://github.com/apereo/cas";
        WebUtils.putLogoutRedirectUrl(request, logoutRedirectUrl);

        strategy.handle(context);
        assertEquals(logoutRedirectUrl, WebUtils.getLogoutRedirectUrl(context, String.class));
    }

    @Test
    public void verifyRedirectToTrustedUrl() {
        val props = new LogoutProperties();
        props.setFollowServiceRedirects(true);

        val request = new MockHttpServletRequest();
        String logoutRedirectUrl = "https://github.com/apereo/cas";
        WebUtils.putLogoutRedirectUrl(request, logoutRedirectUrl);
        val context = getMockRequestContext(request);

        val strategy = new DefaultLogoutRedirectionStrategy(mock(ServiceFactory.class), props, mock(SingleLogoutServiceLogoutUrlBuilder.class));

        strategy.handle(context);
        assertEquals(logoutRedirectUrl, WebUtils.getLogoutRedirectUrl(context, String.class));
    }

    @Test
    public void verifyRedirectToService() {
        val props = new LogoutProperties();
        props.setFollowServiceRedirects(true);
        props.setRedirectParameter("targetParam");

        val request = new MockHttpServletRequest();
        String logoutRedirectUrl = "https://github.com/apereo/cas";
        request.addParameter(props.getRedirectParameter(), logoutRedirectUrl);
        val context = getMockRequestContext(request);

        val logoutUrlBuilder = mock(SingleLogoutServiceLogoutUrlBuilder.class);
        when(logoutUrlBuilder.isServiceAuthorized(any(WebApplicationService.class), any())).thenReturn(Boolean.TRUE);

        val strategy = new DefaultLogoutRedirectionStrategy(new WebApplicationServiceFactory(), props, logoutUrlBuilder);

        strategy.handle(context);
        assertEquals(logoutRedirectUrl, WebUtils.getLogoutRedirectUrl(context, String.class));
    }

    @Test
    public void verifyRedirectToUnauthzService() {
        val props = new LogoutProperties();
        props.setFollowServiceRedirects(true);
        props.setRedirectParameter("targetParam");

        val request = new MockHttpServletRequest();
        String logoutRedirectUrl = "https://github.com/apereo/cas";
        request.addParameter(props.getRedirectParameter(), logoutRedirectUrl);
        val context = getMockRequestContext(request);

        val logoutUrlBuilder = mock(SingleLogoutServiceLogoutUrlBuilder.class);
        when(logoutUrlBuilder.isServiceAuthorized(any(WebApplicationService.class), any())).thenReturn(Boolean.FALSE);

        val strategy = new DefaultLogoutRedirectionStrategy(new WebApplicationServiceFactory(), props, logoutUrlBuilder);

        strategy.handle(context);
        assertNull(WebUtils.getLogoutRedirectUrl(context, String.class));
    }

    @Test
    public void verifyRedirectDisabled() {
        val props = new LogoutProperties();
        props.setFollowServiceRedirects(false);

        val request = new MockHttpServletRequest();
        String service = "https://github.com/apereo/cas";
        request.addParameter(props.getRedirectParameter(), service);
        val context = getMockRequestContext(request);

        val logoutUrlBuilder = mock(SingleLogoutServiceLogoutUrlBuilder.class);
        when(logoutUrlBuilder.isServiceAuthorized(any(WebApplicationService.class), any())).thenReturn(Boolean.TRUE);

        ServiceFactory serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.createService(anyString())).thenReturn(RegisteredServiceTestUtils.getService(service));

        val strategy = new DefaultLogoutRedirectionStrategy(serviceFactory, props, logoutUrlBuilder);

        strategy.handle(context);
        assertNull(WebUtils.getLogoutRedirectUrl(context, String.class));
        assertNotNull(WebUtils.getService(context));
    }

    private static MockRequestContext getMockRequestContext(final MockHttpServletRequest request) {
        val context = new MockRequestContext();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        return context;
    }
}
