package org.apereo.cas.audit.spi.resource;

import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link MessageBundleAwareResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Simple")
public class MessageBundleAwareResourceResolverTests {
    @Test
    public void verifyOperationByExceptionMessage() {
        val jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[]{"something", RegisteredServiceTestUtils.getService()});
        val context = mock(ApplicationContext.class);
        when(context.getMessage(eq("something"), any(),
            eq("RUNTIME_EXCEPTION"), any(Locale.class))).thenReturn("HelloWorld");
        val resolver = new MessageBundleAwareResourceResolver(context);
        var input = resolver.resolveFrom(jp, new RuntimeException("something"));
        assertTrue(input.length > 0);
        assertEquals("HelloWorld", input[0]);
    }
}
