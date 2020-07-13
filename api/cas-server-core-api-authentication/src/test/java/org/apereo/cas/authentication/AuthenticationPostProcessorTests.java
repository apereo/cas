package org.apereo.cas.authentication;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AuthenticationPostProcessorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Authentication")
public class AuthenticationPostProcessorTests {

    @Test
    public void verifyOperation() {
        val p = new AuthenticationPostProcessor() {
            @Override
            public void process(final AuthenticationBuilder builder, final AuthenticationTransaction transaction)
                throws AuthenticationException {
            }
        };
        assertEquals(Ordered.HIGHEST_PRECEDENCE, p.getOrder());
        assertTrue(p.supports(mock(Credential.class)));
    }
}
