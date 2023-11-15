package org.apereo.cas.logout;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultLogoutRedirectionStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Logout")
class DefaultLogoutRedirectionStrategyTests {

    private static MockRequestContext getMockRequestContext() throws Exception {
        val context = MockRequestContext.create();
        context.getHttpServletRequest().setRequestURI(CasProtocolConstants.ENDPOINT_LOGOUT);
        return context;
    }

    @Test
    void verifyNoRedirect() throws Throwable {
        val context = getMockRequestContext();
        val props = new CasConfigurationProperties();
        props.getLogout().setFollowServiceRedirects(true);

        val extractor = new DefaultArgumentExtractor(new LogoutWebApplicationServiceFactory(props.getLogout()));
        val strategy = new DefaultLogoutRedirectionStrategy(extractor, props,
            mock(SingleLogoutServiceLogoutUrlBuilder.class), new WebApplicationServiceFactory());

        assertTrue(strategy.supports(context.getHttpServletRequest(), context.getHttpServletResponse()));
        var response = strategy.handle(context.getHttpServletRequest(), context.getHttpServletResponse());
        assertTrue(response.getLogoutRedirectUrl().isEmpty());

        WebUtils.putLogoutRedirectUrl(context.getHttpServletRequest(), "https://github.com/apereo/cas");
        response = strategy.handle(context.getHttpServletRequest(), context.getHttpServletResponse());
        assertFalse(response.getLogoutRedirectUrl().isEmpty());
    }

    @Test
    void verifyRedirectToTrustedUrl() throws Throwable {
        val context = getMockRequestContext();
        WebUtils.putLogoutRedirectUrl(context.getHttpServletRequest(), "https://github.com/apereo/cas");
        val props = new CasConfigurationProperties();
        props.getLogout().setFollowServiceRedirects(true);
        val extractor = new DefaultArgumentExtractor(new LogoutWebApplicationServiceFactory(props.getLogout()));
        val strategy = new DefaultLogoutRedirectionStrategy(extractor, props,
            mock(SingleLogoutServiceLogoutUrlBuilder.class), new WebApplicationServiceFactory());
        var response = strategy.handle(context.getHttpServletRequest(), context.getHttpServletResponse());
        assertFalse(response.getLogoutRedirectUrl().isEmpty());
    }

    @Test
    void verifyRedirectToService() throws Throwable {
        val props = new CasConfigurationProperties();
        props.getLogout().setFollowServiceRedirects(true).setRedirectParameter(List.of("targetParam"));
        val context = getMockRequestContext();
        context.setParameter("targetParam", "https://github.com/apereo/cas");

        val extractor = new DefaultArgumentExtractor(new LogoutWebApplicationServiceFactory(props.getLogout()));
        val logoutUrlBuilder = mock(SingleLogoutServiceLogoutUrlBuilder.class);
        when(logoutUrlBuilder.isServiceAuthorized(any(WebApplicationService.class), any(), any())).thenReturn(Boolean.TRUE);
        val strategy = new DefaultLogoutRedirectionStrategy(extractor, props, logoutUrlBuilder,
            new WebApplicationServiceFactory());
        var response = strategy.handle(context.getHttpServletRequest(), context.getHttpServletResponse());
        assertFalse(response.getLogoutRedirectUrl().isEmpty());
    }

    @Test
    void verifyRedirectToDefaultServiceInConfig() throws Throwable {
        val props = new CasConfigurationProperties();
        props.getView().setDefaultRedirectUrl("https://google.com");
        props.getLogout().setFollowServiceRedirects(true);
        val context = getMockRequestContext();
        val extractor = new DefaultArgumentExtractor(new LogoutWebApplicationServiceFactory(props.getLogout()));
        val logoutUrlBuilder = mock(SingleLogoutServiceLogoutUrlBuilder.class);
        when(logoutUrlBuilder.isServiceAuthorized(any(WebApplicationService.class), any(), any())).thenReturn(Boolean.TRUE);
        val strategy = new DefaultLogoutRedirectionStrategy(extractor, props, logoutUrlBuilder,
            new WebApplicationServiceFactory());
        var response = strategy.handle(context.getHttpServletRequest(), context.getHttpServletResponse());
        assertFalse(response.getLogoutRedirectUrl().isEmpty());
    }

    @Test
    void verifyRedirectToServiceDisabledInConfig() throws Throwable {
        val props = new CasConfigurationProperties();
        props.getLogout().setFollowServiceRedirects(false).setRedirectParameter(List.of("targetParam"));
        val context = getMockRequestContext();
        context.setParameter("targetParam", "https://github.com/apereo/cas");

        val extractor = new DefaultArgumentExtractor(new LogoutWebApplicationServiceFactory(props.getLogout()));
        val logoutUrlBuilder = mock(SingleLogoutServiceLogoutUrlBuilder.class);
        when(logoutUrlBuilder.isServiceAuthorized(any(WebApplicationService.class), any(), any())).thenReturn(Boolean.TRUE);
        val strategy = new DefaultLogoutRedirectionStrategy(extractor, props, logoutUrlBuilder,
            new WebApplicationServiceFactory());
        var response = strategy.handle(context.getHttpServletRequest(), context.getHttpServletResponse());
        assertFalse(response.getLogoutRedirectUrl().isPresent());
    }

    @Test
    void verifyRedirectToUnauthzService() throws Throwable {
        val props = new CasConfigurationProperties();
        props.getLogout().setFollowServiceRedirects(true).setRedirectParameter(List.of("targetParam"));

        val context = getMockRequestContext();
        context.setParameter("targetParam", "https://github.com/apereo/cas");

        val logoutUrlBuilder = mock(SingleLogoutServiceLogoutUrlBuilder.class);
        val extractor = new DefaultArgumentExtractor(new LogoutWebApplicationServiceFactory(props.getLogout()));
        val strategy = new DefaultLogoutRedirectionStrategy(extractor, props, logoutUrlBuilder,
            new WebApplicationServiceFactory());
        when(logoutUrlBuilder.isServiceAuthorized(any(WebApplicationService.class), any(), any())).thenReturn(Boolean.FALSE);
        var response = strategy.handle(context.getHttpServletRequest(), context.getHttpServletResponse());
        assertFalse(response.getLogoutRedirectUrl().isPresent());
    }
}
