package org.apereo.cas.logging.web;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.spring.DirectObjectProvider;
import org.apereo.cas.web.cookie.CasCookieBuilder;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ThreadContextMDCServletFilterTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Web")
public class ThreadContextMDCServletFilterTests {
    @Test
    public void verifyFilter() throws Exception {
        val request = new MockHttpServletRequest();
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
        request.setAttribute("a1", "v1");
        request.addHeader("h1", "v1");

        val response = new MockHttpServletResponse();
        val filterChain = new MockFilterChain();

        val cookieBuilder = mock(CasCookieBuilder.class);
        val ticketSupport = mock(TicketRegistrySupport.class);
        when(cookieBuilder.retrieveCookieValue(any(HttpServletRequest.class))).thenReturn("TICKET");
        when(ticketSupport.getAuthenticatedPrincipalFrom(anyString())).thenReturn(CoreAuthenticationTestUtils.getPrincipal());
        val filter = new ThreadContextMDCServletFilter(
            new DirectObjectProvider<>(ticketSupport),
            new DirectObjectProvider<>(cookieBuilder));
        filter.init(mock(FilterConfig.class));
        filter.doFilter(request, response, filterChain);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
    }
}
