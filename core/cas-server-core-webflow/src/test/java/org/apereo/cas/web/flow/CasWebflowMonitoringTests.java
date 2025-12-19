package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.config.CasCoreMonitorAutoConfiguration;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.executor.FlowExecutor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasWebflowMonitoringTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("WebflowConfig")
@Import(CasWebflowMonitoringTests.FlowExecutorTestConfiguration.class)
@ImportAutoConfiguration(CasCoreMonitorAutoConfiguration.class)
class CasWebflowMonitoringTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier("testFlowExecutor")
    private FlowExecutor testFlowExecutor;

    @Test
    void verifyOperation() throws Exception {
        val context = MockRequestContext.create(applicationContext);
        val result = testFlowExecutor.launchExecution(CasWebflowConfigurer.FLOW_ID_LOGOUT,
            new LocalAttributeMap<>(), context.getExternalContext());
        assertNull(result);
        val flowId = context.getHttpServletRequest().getAttribute("observingWebflowId");
        assertEquals(CasWebflowConfigurer.FLOW_ID_LOGOUT, flowId);
    }

    @TestConfiguration(value = "FlowExecutorTestConfiguration", proxyBeanMethods = false)
    static class FlowExecutorTestConfiguration {
        @Bean
        public FlowExecutor testFlowExecutor() {
            return mock(FlowExecutor.class);
        }
    }
}
