package org.apereo.cas.logging.web;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreLoggingAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.http.HttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ThreadContextMDCServletFilterTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Web")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreMultitenancyAutoConfiguration.class,
    CasCoreEnvironmentBootstrapAutoConfiguration.class,
    CasCoreCookieAutoConfiguration.class,
    CasCoreLoggingAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
class ThreadContextMDCServletFilterTests {

    @Autowired
    @Qualifier("threadContextMDCServletFilter")
    private FilterRegistrationBean<ThreadContextMDCServletFilter> threadContextMDCServletFilter;

    @Test
    void verifyFilter() throws Throwable {
        assertNotNull(threadContextMDCServletFilter);

        val request = new MockHttpServletRequest();
        assertNotNull(request.getSession(true));

        request.setRequestURI("/cas/login");
        request.setRemoteAddr("1.2.3.4");
        request.setRemoteUser("casuser");
        request.setServerName("serverName");
        request.setServerPort(1000);
        request.setContextPath("ctxpath");
        request.setContentType("contenttype");
        request.setRemotePort(2000);
        request.setQueryString("queryString");
        request.setMethod("method");
        request.setParameter("p1", "v1");
        request.setParameter("password", "helloworld");
        request.setParameter("confirmedPassword", "123456");
        request.setAttribute("a1", "v1");
        request.addHeader("h1", "v1");
        request.addHeader("cookie", "TGC = 12345; SESSION=12345667");

        val response = new MockHttpServletResponse();
        val filterChain = new MockFilterChain();

        val cookieBuilder = mock(CasCookieBuilder.class);
        val ticketSupport = mock(TicketRegistrySupport.class);
        when(cookieBuilder.retrieveCookieValue(any(HttpServletRequest.class))).thenReturn("TICKET");
        when(ticketSupport.getAuthenticatedPrincipalFrom(anyString())).thenReturn(CoreAuthenticationTestUtils.getPrincipal());
        val filter = threadContextMDCServletFilter.getFilter();
        filter.init(mock(FilterConfig.class));
        filter.doFilter(request, response, filterChain);
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        val mdcElements = MDC.getCopyOfContextMap();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertFalse(mdcElements.containsKey("password"));
        assertFalse(mdcElements.containsKey("confirmedPassword"));
        assertFalse(mdcElements.containsKey("cookie"));
        assertNotNull(request.getAttribute("sessionId"));
        assertNotNull(response.getHeader("X-RequestId"));
        assertNotNull(response.getHeader("X-SessionId"));
    }
}
