package org.apereo.cas.adaptors.radius.web.flow;

import org.apereo.cas.web.flow.AbstractMultifactorTrustedDeviceWebflowConfigurer;

/**
 * This is {@link RadiusMultifactorTrustWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RadiusMultifactorTrustWebflowConfigurer extends AbstractMultifactorTrustedDeviceWebflowConfigurer {
    @Override
    protected void doInitialize() throws Exception {
        registerMultifactorTrustedAuthentication();
    }
}
