package org.apereo.cas.web.flow.configurer;

import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowLoginContextProvider;
import org.apereo.cas.web.flow.configurer.plan.DefaultCasWebflowExecutionPlan;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultCasWebflowExecutionPlanTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Webflow")
class DefaultCasWebflowExecutionPlanTests {
    @Test
    void verifyOperation() throws Throwable {
        val plan = new DefaultCasWebflowExecutionPlan();

        val p1 = mock(CasWebflowLoginContextProvider.class);
        when(p1.getOrder()).thenReturn(1);
        when(p1.getName()).thenReturn("P1");
        plan.registerWebflowLoginContextProvider(p1);

        val p2 = mock(CasWebflowLoginContextProvider.class);
        when(p2.getOrder()).thenReturn(0);
        when(p2.getName()).thenReturn("P2");
        plan.registerWebflowLoginContextProvider(p2);

        plan.registerWebflowConfigurer(mock(CasWebflowConfigurer.class));
        assertDoesNotThrow(plan::execute);
        assertEquals(2, plan.getWebflowLoginContextProviders().size());
        assertEquals(1, plan.getWebflowConfigurers().size());
        assertEquals("P2", plan.getWebflowLoginContextProviders().getFirst().getName());
        assertEquals("P1", plan.getWebflowLoginContextProviders().get(1).getName());
    }
}
