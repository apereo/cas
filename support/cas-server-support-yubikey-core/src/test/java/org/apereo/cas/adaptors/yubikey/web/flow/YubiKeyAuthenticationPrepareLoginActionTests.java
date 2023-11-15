package org.apereo.cas.adaptors.yubikey.web.flow;

import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.MockRequestContext;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link YubiKeyAuthenticationPrepareLoginActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowMfaActions")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreHttpConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
class YubiKeyAuthenticationPrepareLoginActionTests extends BaseYubiKeyActionTests {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyActionSuccess() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val action = new YubiKeyAuthenticationPrepareLoginAction(casProperties);
        assertNull(action.execute(context));
    }
}
