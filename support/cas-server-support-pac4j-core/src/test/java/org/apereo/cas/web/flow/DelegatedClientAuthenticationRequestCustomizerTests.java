package org.apereo.cas.web.flow;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DelegatedClientAuthenticationRequestCustomizerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Simple")
public class DelegatedClientAuthenticationRequestCustomizerTests {

    @Test
    public void verifyOperation() {
        val customizer = mock(DelegatedClientAuthenticationRequestCustomizer.class);
        when(customizer.getOrder()).thenCallRealMethod();
        assertEquals(Ordered.LOWEST_PRECEDENCE, customizer.getOrder());
    }
}
