package org.apereo.cas.gauth.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorTokenCredential;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
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
 * This is {@link GoogleAuthenticatorAuthorizeTokenAttemptActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("WebflowMfaActions")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = {
    GoogleAuthenticatorPrepareLoginActionTests.TestMultifactorTestConfiguration.class,
    BaseGoogleAuthenticatorTests.SharedTestConfiguration.class
}, properties = "cas.authn.mfa.gauth.core.maximum-authentication-attempts=3")
class GoogleAuthenticatorAuthorizeTokenAttemptActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_GOOGLE_ACCOUNT_AUTHORIZE_TOKEN_ATTEMPT)
    private Action action;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyOperation() throws Exception {
        val context = MockRequestContext.create(applicationContext);
        val maximumAuthenticationAttempts = casProperties.getAuthn().getMfa().getGauth().getCore().getMaximumAuthenticationAttempts();
        val credential = new GoogleAuthenticatorTokenCredential("234321", 987655L);
        WebUtils.putCredential(context, credential);
        for (var i = 0; i < maximumAuthenticationAttempts; i++) {
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
        }
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, action.execute(context).getId());
    }
}
