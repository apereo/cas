package org.apereo.cas.adaptors.duo.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.trusted.web.flow.AbstractMultifactorTrustedDeviceWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.MultifactorAuthenticationDeviceProviderAction;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link DuoSecurityMultifactorAccountProfileWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
public class DuoSecurityMultifactorAccountProfileWebflowConfigurer extends AbstractMultifactorTrustedDeviceWebflowConfigurer {

    public DuoSecurityMultifactorAccountProfileWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                                 final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                                 final ConfigurableApplicationContext applicationContext,
                                                                 final CasConfigurationProperties casProperties,
                                                                 final List<CasMultifactorWebflowCustomizer> mfaFlowCustomizers) {
        super(flowBuilderServices, loginFlowDefinitionRegistry,
            applicationContext, casProperties, Optional.empty(), mfaFlowCustomizers);
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
