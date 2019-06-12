package org.apereo.cas.audit.spi;

import org.apereo.cas.audit.spi.resource.ShortenedReturnValueAsStringResourceResolver;

import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ShortenedReturnValueAsStringResourceResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class ShortenedReturnValueAsStringResourceResolverTests {
    private final ShortenedReturnValueAsStringResourceResolver r = new ShortenedReturnValueAsStringResourceResolver();

    @Test
    public void verifyActionPassed() {
        val jp = mock(JoinPoint.class);
        assertTrue(r.resolveFrom(jp, RandomStringUtils.randomAlphabetic(52)).length > 0);
    }
}
