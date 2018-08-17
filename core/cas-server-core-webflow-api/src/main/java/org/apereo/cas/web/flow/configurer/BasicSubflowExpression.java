package org.apereo.cas.web.flow.configurer;

import lombok.RequiredArgsConstructor;
import org.springframework.binding.expression.EvaluationException;
import org.springframework.binding.expression.support.AbstractGetValueExpression;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;

/**
 * Creates a custom subflow expression.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@RequiredArgsConstructor
public class BasicSubflowExpression extends AbstractGetValueExpression {
    private final String subflowId;
    private final FlowDefinitionRegistry flowDefinitionRegistry;

    @Override
    public Object getValue(final Object context) throws EvaluationException {
        return this.flowDefinitionRegistry.getFlowDefinition(this.subflowId);
    }
}
