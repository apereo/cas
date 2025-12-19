package org.apereo.cas.web.flow.executor;

import module java.base;
import org.springframework.webflow.context.servlet.FlowUrlHandler;
import org.springframework.webflow.execution.repository.FlowExecutionRepository;
import org.springframework.webflow.executor.FlowExecutor;

/**
 * This is {@link CasFlowExecutor}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public interface CasFlowExecutor extends FlowExecutor {
    /**
     * Gets flow execution repository.
     *
     * @return the flow execution repository
     */
    FlowExecutionRepository getFlowExecutionRepository();

    /**
     * Gets flow url handler.
     *
     * @return the flow url handler
     */
    FlowUrlHandler getFlowUrlHandler();
}
