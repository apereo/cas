package org.apereo.cas.throttle;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ThrottledRequestFilterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("AuthenticationThrottling")
public class ThrottledRequestFilterTests {

    @Test
    public void verifyOperation() {
        val filter = mock(ThrottledRequestFilter.class);
        when(filter.getOrder()).thenCallRealMethod();
        assertEquals(Ordered.HIGHEST_PRECEDENCE, filter.getOrder());
    }
}
