package org.apereo.cas.web.flow;

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

    public OidcWebflowConfigurer(final FlowBuilderServices flowBuilderServices, final Action oidcRegisteredServiceUIAction) {
        super(flowBuilderServices);
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
