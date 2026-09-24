package org.apereo.cas.interrupt.webflow;

import module java.base;
import org.apereo.cas.config.CasInterruptAutoConfiguration;
import org.apereo.cas.config.CasInterruptWebflowAutoConfiguration;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InterruptWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowConfig")
class InterruptWebflowConfigurerTests {
    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration({
        CasInterruptAutoConfiguration.class,
        CasInterruptWebflowAutoConfiguration.class
    })
    public static class SharedTestConfiguration {
    }

    @Import(SharedTestConfiguration.class)
    @TestPropertySource(properties = {
        "cas.interrupt.core.trigger-mode=AFTER_AUTHENTICATION",
        "cas.interrupt.groovy.location=classpath:/interrupt.groovy"
    })
    @Nested
    @Tag("WebflowConfig")
    class InterruptAfterAuthentication extends BaseWebflowConfigurerTests {

        @Test
        void verifyOperation() {
            assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
            val flow = (Flow) this.flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
            assertNotNull(flow);
            assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_FINALIZE_INTERRUPT));
            assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_FINISHED_INTERRUPT));
            assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_INQUIRE_INTERRUPT));
            assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_INTERRUPT_VIEW));
            assertGatewayTransitions(flow, CasWebflowConstants.STATE_ID_INQUIRE_INTERRUPT,
                CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET, CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET);
        }
    }

    @Import(SharedTestConfiguration.class)
    @TestPropertySource(properties = {
        "cas.interrupt.core.trigger-mode=AFTER_SSO",
        "cas.interrupt.groovy.location=classpath:/interrupt.groovy"
    })
    @Nested
    @Tag("WebflowConfig")
    class InterruptAfterSingleSignOn extends BaseWebflowConfigurerTests {
        @Test
        void verifyOperation() {
            assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
            val flow = (Flow) this.flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
            assertNotNull(flow);
            assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_FINALIZE_INTERRUPT));
            assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_FINISHED_INTERRUPT));
            assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_INQUIRE_INTERRUPT));
            assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_INTERRUPT_VIEW));
            assertGatewayTransitions(flow, CasWebflowConstants.STATE_ID_INQUIRE_INTERRUPT, CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET);
        }
    }

    private static void assertGatewayTransitions(final Flow flow, final String... stateIds) {
        for (val stateId : stateIds) {
            val state = (TransitionableState) flow.getState(stateId);
            val transition = state.getTransition(CasWebflowConstants.TRANSITION_ID_GATEWAY);
            assertNotNull(transition, () -> "Missing gateway transition for " + stateId);
            assertEquals(CasWebflowConstants.STATE_ID_GATEWAY_SERVICES_MGMT, transition.getTargetStateId());
        }
    }
}
