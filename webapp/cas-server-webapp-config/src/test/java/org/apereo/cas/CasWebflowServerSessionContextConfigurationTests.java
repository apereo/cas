package org.apereo.cas;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.executor.FlowExecutor;

/**
 * This is {@link CasWebflowServerSessionContextConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@TestPropertySource(properties = "cas.webflow.session.storage=true")
@Slf4j
public class CasWebflowServerSessionContextConfigurationTests extends BaseCasWebflowSessionContextConfiguration {

    @Autowired
    @Qualifier("loginFlowExecutor")
    private FlowExecutor flowExecutorViaServerSessionBindingExecution;

    @Override
    public FlowExecutor getFlowExecutor() {
        return this.flowExecutorViaServerSessionBindingExecution;
    }
}
