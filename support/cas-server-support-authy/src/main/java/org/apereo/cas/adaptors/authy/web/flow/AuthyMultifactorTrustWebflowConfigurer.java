package org.apereo.cas.adaptors.authy.web.flow;

import org.apereo.cas.web.flow.AbstractMultifactorTrustedDeviceWebflowConfigurer;

/**
 * This is {@link AuthyMultifactorTrustWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class AuthyMultifactorTrustWebflowConfigurer extends AbstractMultifactorTrustedDeviceWebflowConfigurer {
    
    @Override
    protected void doInitialize() throws Exception {
        registerMultifactorTrustedAuthentication();
    }

}
