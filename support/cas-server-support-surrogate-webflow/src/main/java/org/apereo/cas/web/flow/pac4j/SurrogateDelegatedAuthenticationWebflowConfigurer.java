package org.apereo.cas.web.flow.pac4j;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link SurrogateDelegatedAuthenticationWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public class SurrogateDelegatedAuthenticationWebflowConfigurer extends AbstractCasWebflowConfigurer {

    public SurrogateDelegatedAuthenticationWebflowConfigurer(
        final FlowBuilderServices flowBuilderServices,
        final FlowDefinitionRegistry flowDefinitionRegistry,
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
        setOrder(Ordered.LOWEST_PRECEDENCE);
    }


    @Override
    public void postInitialization(final ConfigurableApplicationContext applicationContext) {
        val allow = casProperties.getAuthn().getPac4j().getCore().isAllowImpersonation();
        if (allow) {
            val flow = getLoginFlow();
            val state = getState(flow, CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION, ActionState.class);
            createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_LOAD_SURROGATES_ACTION, true);
            state.getExitActionList().add(new ConsumerExecutionAction(context -> {
                if (context.getCurrentState().getId().equals(CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION)
                    && context.getCurrentEvent().getId().equals(CasWebflowConstants.TRANSITION_ID_SUCCESS)) {
                    WebUtils.putSurrogateAuthenticationRequest(context, Boolean.TRUE);
                }
            }));
        }
    }
}
