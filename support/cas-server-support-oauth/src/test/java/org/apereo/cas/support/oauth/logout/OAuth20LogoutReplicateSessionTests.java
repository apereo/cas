package org.apereo.cas.support.oauth.logout;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.logout.LogoutExecutionPlan;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20LogoutReplicateSessionTests}.
 *
 * @author Jerome Leleu
 * @since 6.5.0
 */
@SpringBootTest(classes = AbstractOAuth20Tests.SharedTestConfiguration.class,
        properties = {
                "cas.authn.attribute-repository.stub.attributes.uid=cas",
                "cas.authn.attribute-repository.stub.attributes.givenName=apereo-cas",
                "spring.main.allow-bean-definition-overriding=true",
                "cas.authn.oauth.replicate-sessions=true"
        })
@EnableTransactionManagement
@EnableAspectJAutoProxy
@Tag("OAuth")
public class OAuth20LogoutReplicateSessionTests {

    @Autowired
    @Qualifier("logoutExecutionPlan")
    private LogoutExecutionPlan logoutExecutionPlan;

    @Test
    public void verifyThatTheOAuthSpecificLogoutPostProcessorIsRegistered() throws Exception {

        assertEquals(1, logoutExecutionPlan.getLogoutPostProcessors().size());
    }
}
