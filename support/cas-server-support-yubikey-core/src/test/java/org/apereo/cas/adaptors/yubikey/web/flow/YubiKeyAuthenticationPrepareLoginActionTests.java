package org.apereo.cas.adaptors.yubikey.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.MockRequestContext;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link YubiKeyAuthenticationPrepareLoginActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowMfaActions")
class YubiKeyAuthenticationPrepareLoginActionTests extends BaseYubiKeyActionTests {

    @Test
    void verifyActionSuccess() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val casProperties = new CasConfigurationProperties();
        val action = new YubiKeyAuthenticationPrepareLoginAction(casProperties);
        assertNull(action.execute(context));
    }
}
