package org.apereo.cas.adaptors.yubikey.web.flow;

import org.apereo.cas.web.flow.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.flow.AbstractMultifactorTrustedDeviceWebflowConfigurer;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;

/**
 * This is {@link YubiKeyMultifactorTrustWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class YubiKeyMultifactorTrustWebflowConfigurer extends AbstractMultifactorTrustedDeviceWebflowConfigurer {
    
    @Override
    protected void doInitialize() throws Exception {
        registerMultifactorTrustedAuthentication();
    }
    
}

