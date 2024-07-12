package org.apereo.cas.adaptors.yubikey.web.flow;

import org.apereo.cas.adaptors.yubikey.BaseYubiKeyTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link YubiKeyAuthenticationPrepareLoginActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowMfaActions")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseYubiKeyTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.yubikey.client-id=18423",
        "cas.authn.mfa.yubikey.secret-key=zAIqhjui12mK8x82oe9qzBEb0As="
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
class YubiKeyAuthenticationPrepareLoginActionTests extends BaseYubiKeyActionTests {
    
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_YUBIKEY_PREPARE_LOGIN)
    private Action prepareYubiKeyAuthenticationLoginAction;

    @Test
    void verifyAction() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        assertNull(prepareYubiKeyAuthenticationLoginAction.execute(context));
        assertTrue(MultifactorAuthenticationWebflowUtils.isMultifactorDeviceRegistrationEnabled(context));
    }

    @Test
    void verifyRegistrationDisabled() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        MultifactorAuthenticationWebflowUtils.putMultifactorDeviceRegistrationEnabled(context, false);
        assertNull(prepareYubiKeyAuthenticationLoginAction.execute(context));
        assertFalse(MultifactorAuthenticationWebflowUtils.isMultifactorDeviceRegistrationEnabled(context));
    }
}
