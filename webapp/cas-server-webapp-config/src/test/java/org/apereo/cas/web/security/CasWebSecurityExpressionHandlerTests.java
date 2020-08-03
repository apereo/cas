package org.apereo.cas.web.security;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasWebSecurityExpressionHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("WebApp")
public class CasWebSecurityExpressionHandlerTests {

    @Test
    public void verifyOperation() {
        val handler = new CasWebSecurityExpressionHandler();
        val root = handler.createSecurityExpressionRoot(mock(Authentication.class), mock(FilterInvocation.class));
        assertTrue(root instanceof CasWebSecurityExpressionRoot);
    }
}
