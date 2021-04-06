package org.apereo.cas.interrupt.webflow;

import org.apereo.cas.config.CasInterruptConfiguration;
import org.apereo.cas.config.CasInterruptWebflowConfiguration;
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
public class InterruptWebflowConfigurerTests {
    @ImportAutoConfiguration({
        AopAutoConfiguration.class,
        RefreshAutoConfiguration.class
    })
    @Import({
        CasInterruptConfiguration.class,
        CasInterruptWebflowConfiguration.class,
        BaseWebflowConfigurerTests.SharedTestConfiguration.class
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
    @SuppressWarnings("ClassCanBeStatic")
    public class InterruptAfterAuthentication extends BaseWebflowConfigurerTests {

        @Test
        public void verifyOperation() {
            assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
            val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
            assertNotNull(flow);
            assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_FINALIZE_INTERRUPT_ACTION));
            assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_FINISHED_INTERRUPT));
            assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_INQUIRE_INTERRUPT_ACTION));
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
    @SuppressWarnings("ClassCanBeStatic")
    public class InterruptAfterSingleSignOn extends BaseWebflowConfigurerTests {
        @Test
        public void verifyOperation() {
            assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
            val flow = (Flow) this.loginFlowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
            assertNotNull(flow);
            assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_FINALIZE_INTERRUPT_ACTION));
            assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_FINISHED_INTERRUPT));
            assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_INQUIRE_INTERRUPT_ACTION));
            assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_INTERRUPT_VIEW));
        }
    }
}
