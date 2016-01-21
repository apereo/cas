package org.jasig.cas.adaptors.yubikey.web.flow;

import org.jasig.cas.web.flow.AbstractCasWebflowConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;

/**
 * This is {@link YubiKeyMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("yubikeyMultifactorWebflowConfigurer")
public class YubiKeyMultifactorWebflowConfigurer extends AbstractCasWebflowConfigurer {

    /** Duo Webflow event id. */
    public static final String MFA_YUBIKEY_EVENT_ID = "mfa-yubikey";

    @Autowired
    @Qualifier("yubikeyFlowRegistry")
    private FlowDefinitionRegistry yubikeyFlowRegistry;

    @Override
    protected void doInitialize() throws Exception {
        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), MFA_YUBIKEY_EVENT_ID, this.yubikeyFlowRegistry);
    }
}
