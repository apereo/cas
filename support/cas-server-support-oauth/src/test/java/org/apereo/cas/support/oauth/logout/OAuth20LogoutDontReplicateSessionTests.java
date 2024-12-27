package org.apereo.cas.support.oauth.logout;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.logout.LogoutExecutionPlan;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20LogoutDontReplicateSessionTests}.
 *
 * @author Jerome Leleu
 * @since 6.5.0
 */
@Tag("OAuth")
@TestPropertySource(properties = {
    "cas.ticket.track-descendant-tickets=false",
    "cas.authn.oauth.session-replication.replicate-sessions=false"
})
class OAuth20LogoutDontReplicateSessionTests extends AbstractOAuth20Tests {

    @Autowired
    @Qualifier(LogoutExecutionPlan.BEAN_NAME)
    private LogoutExecutionPlan logoutExecutionPlan;

    @Test
    void verifyThatTheOAuthSpecificLogoutPostProcessorIsNotRegistered() {
        assertEquals(0, logoutExecutionPlan.getLogoutPostProcessors().size());
    }
}
