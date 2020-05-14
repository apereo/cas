package org.apereo.cas.web.security;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
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
@SpringBootTest(classes = {
    AopAutoConfiguration.class,
    RefreshAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Simple")
public class CasWebSecurityExpressionHandlerTests {

    @Test
    public void verifyOperation() {
        val handler = new CasWebSecurityExpressionHandler();
        val root = handler.createSecurityExpressionRoot(mock(Authentication.class), mock(FilterInvocation.class));
        assertTrue(root instanceof CasWebSecurityExpressionRoot);
    }
}
