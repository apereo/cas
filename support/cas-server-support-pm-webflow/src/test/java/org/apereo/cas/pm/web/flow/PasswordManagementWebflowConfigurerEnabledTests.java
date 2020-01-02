package org.apereo.cas.pm.web.flow;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionableState;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PasswordManagementWebflowConfigurerEnabledTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@TestPropertySource(properties = "cas.authn.pm.enabled=true")
@Tag("Webflow")
public class PasswordManagementWebflowConfigurerEnabledTests extends PasswordManagementWebflowConfigurerDisabledTests {
    @Override
    protected void verifyPasswordManagementStates(final Flow flow) {
        super.verifyPasswordManagementStates(flow);
        var state = (TransitionableState) flow.getState(PasswordManagementWebflowConfigurer.STATE_ID_PASSWORD_CHANGE_ACTION);
        assertNotNull(state);
    }
}

