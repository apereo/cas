package org.apereo.cas.web.flow;

import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
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
 * This is {@link InitializeCaptchaActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseCaptchaConfigurationTests.SharedTestConfiguration.class,
    properties = {
        "cas.google-recaptcha.verify-url=http://localhost:9294",
        "cas.google-recaptcha.site-key=6LauELajSYtaX8",
        "cas.google-recaptcha.secret=6L9LlZyI10_X4LV",
        "cas.google-recaptcha.version=GOOGLE_RECAPTCHA_V3",
        "cas.google-recaptcha.enabled=true"
    }
)
@Tag("WebflowActions")
@ExtendWith(CasTestExtension.class)
class InitializeCaptchaActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_INIT_CAPTCHA)
    private Action initializeCaptchaAction;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyCaptchaValidated() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, initializeCaptchaAction.execute(context).getId());
        assertNotNull(WebUtils.getRecaptchaSiteKey(context));
        assertTrue(context.getFlowScope().contains("recaptchaLoginEnabled"));
    }
}
