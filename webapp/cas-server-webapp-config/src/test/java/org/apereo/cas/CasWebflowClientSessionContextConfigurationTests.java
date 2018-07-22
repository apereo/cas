package org.apereo.cas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.executor.FlowExecutor;

/**
 * This is {@link CasWebflowClientSessionContextConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CasWebflowClientSessionContextConfigurationTests extends BaseCasWebflowSessionContextConfigurationTests {

    @Autowired
    @Qualifier("loginFlowExecutor")
    private FlowExecutor flowExecutorViaClientFlowExecution;

    @Override
    public FlowExecutor getFlowExecutor() {
        return this.flowExecutorViaClientFlowExecution;
    }
}
