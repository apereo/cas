package org.apereo.cas.adaptors.radius.web.flow;

import org.apereo.cas.web.flow.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.flow.AbstractMultifactorTrustedDeviceWebflowConfigurer;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;

/**
 * This is {@link RadiusMultifactorTrustWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RadiusMultifactorTrustWebflowConfigurer extends AbstractMultifactorTrustedDeviceWebflowConfigurer {
    @Override
    protected void doInitialize() throws Exception {
        registerMultifactorTrustedAuthentication();
    }
}
