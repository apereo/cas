package org.jasig.cas.adaptors.duo.web.flow;

import org.jasig.cas.web.flow.AbstractCasWebflowConfigurer;
import org.jasig.cas.web.flow.CasWebflowConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.TransitionableState;

/**
 * This is {@link DuoMultifactorWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("duoMultifactorWebflowConfigurer")
public class DuoMultifactorWebflowConfigurer extends AbstractCasWebflowConfigurer {

    /** Duo Webflow event id. */
    public static final String MFA_DUO_EVENT_ID = "mfa-duo";

    @Autowired
    @Qualifier("duoFlowRegistry")
    private FlowDefinitionRegistry duoFlowRegistry;

    @Override
    protected void doInitialize() throws Exception {
        registerMultifactorProviderAuthenticationWebflow(getLoginFlow(), MFA_DUO_EVENT_ID, this.duoFlowRegistry);

    }
}
