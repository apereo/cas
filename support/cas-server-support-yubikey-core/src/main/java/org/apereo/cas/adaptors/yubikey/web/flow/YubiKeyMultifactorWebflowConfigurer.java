package org.apereo.cas.adaptors.yubikey.web.flow;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasMultifactorWebflowConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link YubiKeyMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class YubiKeyMultifactorWebflowConfigurer extends AbstractCasMultifactorWebflowConfigurer {

    /** Webflow event id. */
    public static final String MFA_YUBIKEY_EVENT_ID = "mfa-yubikey";
    
    private final FlowDefinitionRegistry yubikeyFlowRegistry;

    public YubiKeyMultifactorWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                               final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                               final FlowDefinitionRegistry flowDefinitionRegistry,
                                               final ApplicationContext applicationContext,
                                               final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        this.yubikeyFlowRegistry = flowDefinitionRegistry;
    }

    @Override
    protected void doInitialize() {
        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), MFA_YUBIKEY_EVENT_ID,
                this.yubikeyFlowRegistry, casProperties.getAuthn().getMfa().getYubikey().getId());
    }
}
