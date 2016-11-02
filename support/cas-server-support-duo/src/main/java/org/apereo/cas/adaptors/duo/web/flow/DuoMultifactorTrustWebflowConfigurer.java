package org.apereo.cas.adaptors.duo.web.flow;

import org.apereo.cas.web.flow.AbstractMultifactorTrustedDeviceWebflowConfigurer;

/**
 * This is {@link DuoMultifactorTrustWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DuoMultifactorTrustWebflowConfigurer extends AbstractMultifactorTrustedDeviceWebflowConfigurer {


    @Override
    protected void doInitialize() throws Exception {
        registerMultifactorTrustedAuthentication();
    }

}
