package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.Flow;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedAuthenticationWebflowConfigurerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Import(BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
@Tag("WebflowConfig")
@TestPropertySource(properties = "cas.authn.pac4j.core.discovery-selection.selection-type=DYNAMIC")
class DelegatedAuthenticationWebflowConfigurerTests extends BaseWebflowConfigurerTests {
    @Test
    void verifyOperation() {
        assertFalse(casWebflowExecutionPlan.getWebflowConfigurers().isEmpty());
        val flow = (Flow) this.flowDefinitionRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_LOGIN);
        assertNotNull(flow);
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_CLIENT_RETRY));
        assertTrue(flow.containsState(CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION));
    }

    @TestConfiguration(value = "DelegatedTestConfiguration", proxyBeanMethods = false)
    static class DelegatedTestConfiguration {
        @Bean
        public DelegatedClientWebflowCustomizer surrogateDuoSecurityMultifactorWebflowCustomizer() {
            return BeanSupplier.of(DelegatedClientWebflowCustomizer.class)
                .otherwiseProxy().get();
        }
    }
}
