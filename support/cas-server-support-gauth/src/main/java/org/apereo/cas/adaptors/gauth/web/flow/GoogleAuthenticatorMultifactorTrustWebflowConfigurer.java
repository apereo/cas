package org.apereo.cas.adaptors.gauth.web.flow;

import org.apereo.cas.web.flow.AbstractMultifactorTrustedDeviceWebflowConfigurer;

/**
 * This is {@link GoogleAuthenticatorMultifactorTrustWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GoogleAuthenticatorMultifactorTrustWebflowConfigurer extends AbstractMultifactorTrustedDeviceWebflowConfigurer {


    @Override
    protected void doInitialize() throws Exception {
        registerMultifactorTrustedAuthentication();
    }

}
