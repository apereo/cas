package org.apereo.cas.web.saml2;

import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.saml.state.SAML2StateGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedSaml2ClientTerminateSessionActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseSaml2DelegatedAuthenticationTests.SharedTestConfiguration.class,
    properties = "cas.authn.pac4j.core.session-replication.replicate-sessions=false")
@Tag("Delegation")
class DelegatedSaml2ClientTerminateSessionActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_SAML2_TERMINATE_SESSION)
    private Action delegatedSaml2ClientTerminateSessionAction;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("delegatedClientDistributedSessionStore")
    private SessionStore delegatedClientDistributedSessionStore;

    @Test
    void verifyOperation() throws Exception {
        val context = MockRequestContext.create(applicationContext);
        val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());
        val manager = new ProfileManager(webContext, delegatedClientDistributedSessionStore);
        val profile = new CommonProfile();
        profile.setClientName("SAML2Client");
        manager.save(true, profile, false);
        val results = delegatedSaml2ClientTerminateSessionAction.execute(context);
        assertNull(results);
        val relayState = delegatedClientDistributedSessionStore.get(webContext, SAML2StateGenerator.SAML_RELAY_STATE_ATTRIBUTE);
        assertEquals("SAML2Client", relayState.orElseThrow().toString());
    }

}
