package org.apereo.cas.oidc.web.flow;

import org.apereo.cas.web.flow.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link OidcWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OidcWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private final Action oidcRegisteredServiceUIAction;

    public OidcWebflowConfigurer(final FlowBuilderServices flowBuilderServices, final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                 final Action oidcRegisteredServiceUIAction) {
        super(flowBuilderServices, loginFlowDefinitionRegistry);
        this.oidcRegisteredServiceUIAction = oidcRegisteredServiceUIAction;
    }

    @Override
    protected void doInitialize() throws Exception {
        final Flow loginFlow = getLoginFlow();
        if (loginFlow != null) {
            final ViewState state = (ViewState) loginFlow.getTransitionableState(CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
            state.getEntryActionList().add(this.oidcRegisteredServiceUIAction);
        }
    }
}
