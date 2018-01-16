package org.apereo.cas;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.executor.FlowExecutor;

/**
 * This is {@link CasWebflowClientSessionContextConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class CasWebflowClientSessionContextConfigurationTests extends BaseCasWebflowSessionContextConfiguration {

    @Autowired
    @Qualifier("loginFlowExecutor")
    private FlowExecutor flowExecutorViaClientFlowExecution;

    @Override
    public FlowExecutor getFlowExecutor() {
        return this.flowExecutorViaClientFlowExecution;
    }
}
