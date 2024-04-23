package org.apereo.cas.support.sms;

import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasTextMagicSmsAutoConfiguration;
import org.apereo.cas.notifications.sms.SmsSender;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TextMagicSmsConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasTextMagicSmsAutoConfiguration.class
})
@Tag("SMS")
class TextMagicSmsConfigurationTests {
    @Autowired
    @Qualifier(SmsSender.BEAN_NAME)
    private SmsSender smsSender;

    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(smsSender);
    }
}
