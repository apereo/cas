package org.apereo.cas.web.flow.configurer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.binding.expression.EvaluationException;
import org.springframework.binding.expression.support.AbstractGetValueExpression;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;

/**
 * Creates a custom subflow expression.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
public class BasicSubflowExpression extends AbstractGetValueExpression {
    private final String subflowId;
    private final FlowDefinitionRegistry flowDefinitionRegistry;

    /**
     * Instantiates a new Basic subflow expression.
     *
     * @param subflowId the subflow id
     */
    BasicSubflowExpression(final String subflowId, final FlowDefinitionRegistry definitionRegistry) {
        this.subflowId = subflowId;
        this.flowDefinitionRegistry = definitionRegistry;
    }

    @Override
    public Object getValue(final Object context) throws EvaluationException {
        return this.flowDefinitionRegistry.getFlowDefinition(this.subflowId);
    }
}
