package org.apereo.cas.adaptors.yubikey.web.flow;

import org.apereo.cas.web.flow.AbstractMultifactorTrustedDeviceWebflowConfigurer;

/**
 * This is {@link YubiKeyMultifactorTrustWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class YubiKeyMultifactorTrustWebflowConfigurer extends AbstractMultifactorTrustedDeviceWebflowConfigurer {
    
    @Override
    protected void doInitialize() throws Exception {
        registerMultifactorTrustedAuthentication();
    }
    
}

