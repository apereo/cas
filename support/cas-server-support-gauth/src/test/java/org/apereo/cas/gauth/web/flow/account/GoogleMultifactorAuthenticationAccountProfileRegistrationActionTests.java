package org.apereo.cas.gauth.web.flow.account;

import module java.base;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GoogleMultifactorAuthenticationAccountProfileRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = {
    BaseGoogleAuthenticatorTests.SharedTestConfiguration.class,
    CasCoreWebflowAutoConfiguration.class
},
    properties = "CasFeatureModule.AccountManagement.enabled=true")
@Tag("WebflowMfaActions")
@ExtendWith(CasTestExtension.class)
class GoogleMultifactorAuthenticationAccountProfileRegistrationActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_GOOGLE_MFA_REGISTRATION)
    private Action googleAccountProfileRegistrationAction;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Exception {
        val context = MockRequestContext.create(applicationContext);
        val eventId = googleAccountProfileRegistrationAction.execute(context);
        assertNull(eventId);
        assertNotNull(MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationProvider(context));
    }
}
