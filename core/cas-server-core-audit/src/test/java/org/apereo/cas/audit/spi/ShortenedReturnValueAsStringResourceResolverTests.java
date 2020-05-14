package org.apereo.cas.audit.spi;

import org.apereo.cas.audit.spi.resource.ShortenedReturnValueAsStringResourceResolver;
import org.apereo.cas.util.RandomUtils;

import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ShortenedReturnValueAsStringResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Simple")
public class ShortenedReturnValueAsStringResourceResolverTests {
    private final ShortenedReturnValueAsStringResourceResolver r = new ShortenedReturnValueAsStringResourceResolver();

    @Test
    public void verifyActionPassed() {
        val jp = mock(JoinPoint.class);
        assertTrue(r.resolveFrom(jp, RandomUtils.randomAlphabetic(52)).length > 0);
    }
}
