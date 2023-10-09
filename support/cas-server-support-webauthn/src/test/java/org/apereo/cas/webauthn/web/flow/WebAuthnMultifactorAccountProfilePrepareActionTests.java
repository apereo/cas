package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.config.CasWebflowAccountProfileConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.execution.Action;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WebAuthnMultifactorAccountProfilePrepareActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("WebflowMfaActions")
@Import(CasWebflowAccountProfileConfiguration.class)
@SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class,
    properties = "CasFeatureModule.AccountManagement.enabled=true")
public class WebAuthnMultifactorAccountProfilePrepareActionTests {

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_WEBAUTHN_MFA_PREPARE)
    private Action webAuthnAccountProfilePrepareAction;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Exception {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString()), context);
        val eventId = webAuthnAccountProfilePrepareAction.execute(context);
        assertNull(eventId);
        assertNotNull(WebUtils.getMultifactorAuthenticationProvider(context));
        assertTrue(context.getFlowScope().contains("webauthnAccountProfileRegistrationEnabled"));
    }

}
