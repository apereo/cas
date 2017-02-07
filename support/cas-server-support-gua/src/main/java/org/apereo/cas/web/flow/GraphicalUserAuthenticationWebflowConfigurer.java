package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.model.support.gua.GraphicalUserAuthenticationProperties;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link GraphicalUserAuthenticationWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class GraphicalUserAuthenticationWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private final GraphicalUserAuthenticationProperties guaProperties;

    public GraphicalUserAuthenticationWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                        final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                        final GraphicalUserAuthenticationProperties gua) {
        super(flowBuilderServices, loginFlowDefinitionRegistry);
        this.guaProperties = gua;
    }

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();
        if (flow != null) {
            final ViewState state = (ViewState) flow.getState(CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
            state.getEntryActionList().add(createEvaluateAction("prepareForGraphicalAuthenticationAction"));

        }
    }
}
