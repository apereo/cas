package org.apereo.cas.interrupt.webflow;

import org.apereo.cas.config.CasInterruptAutoConfiguration;
import org.apereo.cas.config.CasInterruptWebflowAutoConfiguration;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.Flow;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InterruptWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowConfig")
class InterruptWebflowConfigurerTests {
    @ImportAutoConfiguration({
        AopAutoConfiguration.class,
        RefreshAutoConfiguration.class
    })
    @Import({
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
        void verifyOperation() throws Throwable {
            assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
            val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
            assertNotNull(flow);
            assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_FINALIZE_INTERRUPT));
            assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_FINISHED_INTERRUPT));
            assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_INQUIRE_INTERRUPT));
            assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_INTERRUPT_VIEW));
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
        void verifyOperation() throws Throwable {
            assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
            val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
            assertNotNull(flow);
            assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_FINALIZE_INTERRUPT));
            assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_FINISHED_INTERRUPT));
            assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_INQUIRE_INTERRUPT));
            assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_INTERRUPT_VIEW));
        }
    }
}
