package org.apereo.cas.support.saml.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.engine.support.TransitionExecutingFlowExecutionExceptionHandler;

/**
 * This is {@link SamlIdPWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlIdPWebflowConfigurer extends AbstractCasWebflowConfigurer {

    public SamlIdPWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                    final FlowDefinitionRegistry flowDefinitionRegistry,
                                    final ConfigurableApplicationContext applicationContext,
                                    final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            val state = getTransitionableState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, ViewState.class);
            state.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_SAML_IDP_METADATA_UI_PARSER));
            val handler = new TransitionExecutingFlowExecutionExceptionHandler();
            handler.add(SamlException.class, CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK);
            flow.getExceptionHandlerSet().add(handler);
        }
    }
}
