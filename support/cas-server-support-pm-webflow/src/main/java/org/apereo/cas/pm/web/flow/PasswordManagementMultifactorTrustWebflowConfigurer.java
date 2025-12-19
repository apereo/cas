package org.apereo.cas.pm.web.flow;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.trusted.util.MultifactorAuthenticationTrustUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link PasswordManagementMultifactorTrustWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public class PasswordManagementMultifactorTrustWebflowConfigurer extends AbstractCasWebflowConfigurer {
    public PasswordManagementMultifactorTrustWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                               final FlowDefinitionRegistry flowDefinitionRegistry,
                                                               final ConfigurableApplicationContext applicationContext,
                                                               final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
        setOrder(casProperties.getAuthn().getPm().getWebflow().getOrder() + 1);
    }

    @Override
    protected void doInitialize() {
        val pm = casProperties.getAuthn().getPm();
        if (pm.getCore().isEnabled()) {
            val pswdFlow = getFlow(FLOW_ID_PASSWORD_RESET);
            if (containsFlowState(pswdFlow, CasWebflowConstants.STATE_ID_INIT_PASSWORD_RESET)) {
                val initReset = getState(pswdFlow, CasWebflowConstants.STATE_ID_INIT_PASSWORD_RESET);
                initReset.getExitActionList().add(new ConsumerExecutionAction(requestContext -> {
                    MultifactorAuthenticationWebflowUtils.putMultifactorDeviceRegistrationEnabled(requestContext, false);
                    MultifactorAuthenticationTrustUtils.putMultifactorAuthenticationTrustedDevicesDisabled(requestContext, true);
                }));
            }
        }
    }
}
