package org.apereo.cas.web.flow.configurer;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.MultifactorAuthenticationDeviceProviderAction;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.util.ArrayList;

/**
 * This is {@link MultifactorAuthenticationAccountProfileWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public class MultifactorAuthenticationAccountProfileWebflowConfigurer extends AbstractCasWebflowConfigurer {
    public MultifactorAuthenticationAccountProfileWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                                    final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                                    final ConfigurableApplicationContext applicationContext,
                                                                    final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        setOrder(Ordered.LOWEST_PRECEDENCE);
    }

    @Override
    protected void doInitialize() {
        val flow = getFlow(CasWebflowConfigurer.FLOW_ID_ACCOUNT);
        if (flow != null) {
            val providerActions = new ArrayList<>(applicationContext.getBeansOfType(MultifactorAuthenticationDeviceProviderAction.class).values());
            AnnotationAwareOrderComparator.sort(providerActions);
            val accountView = getState(flow, CasWebflowConstants.STATE_ID_MY_ACCOUNT_PROFILE_VIEW, ViewState.class);
            accountView.getRenderActionList().addAll(providerActions.toArray(new Action[]{}));
        }
    }
}
