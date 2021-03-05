package org.apereo.cas.web.security;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasWebSecurityExpressionRootTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("WebApp")
public class CasWebSecurityExpressionRootTests {

    @Test
    public void verifyOperation() {
        val invocation = mock(FilterInvocation.class);
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("249.104.155.200");
        when(invocation.getRequest()).thenReturn(request);
        val root = new CasWebSecurityExpressionRoot(mock(Authentication.class), invocation);
        assertTrue(root.hasIpAddress(request.getRemoteAddr()));
        assertTrue(root.hasIpAddress("^249.10.+00"));
        assertFalse(root.hasIpAddress("^249.+300"));
    }
}
