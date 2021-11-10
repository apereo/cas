package org.apereo.cas.support.oauth.logout;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.logout.LogoutExecutionPlan;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This is {@link OAuth20LogoutDontReplicateSessionTests}.
 *
 * @author Jerome Leleu
 * @since 6.5.0
 */
@Tag("OAuth")
public class OAuth20LogoutDontReplicateSessionTests extends AbstractOAuth20Tests {

    @Autowired
    @Qualifier("logoutExecutionPlan")
    private LogoutExecutionPlan logoutExecutionPlan;

    @Test
    public void verifyThatTheOAuthSpecificLogoutPostProcessorIsNotRegistered() throws Exception {

        assertEquals(0, logoutExecutionPlan.getLogoutPostProcessors().size());
    }
}
