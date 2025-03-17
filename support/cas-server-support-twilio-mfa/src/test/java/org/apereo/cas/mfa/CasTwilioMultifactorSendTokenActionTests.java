package org.apereo.cas.mfa;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasTwilioMultifactorSendTokenActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("WebflowActions")
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@SpringBootTest(
    classes = BaseTwilioMultifactorTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.twilio.core.account-id=${#randomString8}",
        "cas.authn.mfa.twilio.core.token=${#randomString8}",
        "cas.authn.mfa.twilio.core.service-sid=${#randomString8}"
    })
class CasTwilioMultifactorSendTokenActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_MFA_TWILIO_SEND_TOKEN)
    private Action mfaTwilioMultifactorSendTokenAction;

    @Autowired
    @Qualifier("casTwilioMultifactorAuthenticationProvider")
    private MultifactorAuthenticationProvider casTwilioMultifactorAuthenticationProvider;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Exception {
        val requestContext = MockRequestContext.create(applicationContext);
        WebUtils.putServiceIntoFlashScope(requestContext, RegisteredServiceTestUtils.getService());
        MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(requestContext, casTwilioMultifactorAuthenticationProvider);
        val principal = RegisteredServiceTestUtils.getPrincipal("casuser", Map.of("phoneNumber", List.of("+13247463546")));
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(principal), requestContext);
        val result = mfaTwilioMultifactorSendTokenAction.execute(requestContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());
    }
}
