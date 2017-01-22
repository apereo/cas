package org.apereo.cas.support.saml.web.flow;

import org.apereo.cas.web.flow.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link SamlIdPMetadataUIWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlIdPMetadataUIWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private final Action samlMetadataUIParserAction;

    public SamlIdPMetadataUIWebflowConfigurer(final FlowBuilderServices flowBuilderServices, final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                              final Action samlMetadataUIParserAction) {
        super(flowBuilderServices, loginFlowDefinitionRegistry);
        this.samlMetadataUIParserAction = samlMetadataUIParserAction;
    }

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();
        if (flow != null) {
            final ViewState state = (ViewState) flow.getTransitionableState(CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
            state.getEntryActionList().add(this.samlMetadataUIParserAction);
        }
    }
}
