package org.apereo.cas.web.flow.configurer;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasMultifactorWebflowCustomizerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Webflow")
class CasMultifactorWebflowCustomizerTests {

    @Test
    void verifyOperation() {
        val input = mock(CasMultifactorWebflowCustomizer.class);
        when(input.getCandidateStatesForMultifactorAuthentication()).thenCallRealMethod();
        when(input.getOrder()).thenCallRealMethod();
        when(input.getWebflowAttributeMappings()).thenCallRealMethod();

        assertTrue(input.getCandidateStatesForMultifactorAuthentication().isEmpty());
        assertEquals(Ordered.LOWEST_PRECEDENCE, input.getOrder());
        assertTrue(input.getWebflowAttributeMappings().isEmpty());
    }

    @Test
    void verifyDefaultOperation() {
        val input = mock(CasWebflowCustomizer.class);
        when(input.getOrder()).thenCallRealMethod();
        when(input.getWebflowAttributeMappings()).thenCallRealMethod();
        assertEquals(Ordered.LOWEST_PRECEDENCE, input.getOrder());
        assertTrue(input.getWebflowAttributeMappings().isEmpty());
    }
}
