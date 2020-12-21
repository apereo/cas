package org.apereo.cas.adaptors.yubikey.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link YubiKeyAuthenticationPrepareLoginActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowMfaActions")
public class YubiKeyAuthenticationPrepareLoginActionTests {
    @Test
    public void verifyActionSuccess() throws Exception {
        val context = new MockRequestContext();
        val casProperties = new CasConfigurationProperties();
        val action = new YubiKeyAuthenticationPrepareLoginAction(casProperties);
        assertNull(action.execute(context));
    }
}
