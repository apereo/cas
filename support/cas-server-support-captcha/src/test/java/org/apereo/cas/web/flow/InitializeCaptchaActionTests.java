package org.apereo.cas.web.flow;

import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.webflow.context.ExternalContextHolder.setExternalContext;
import static org.springframework.webflow.execution.RequestContextHolder.setRequestContext;

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
        "cas.google-recaptcha.version=V3"
    }
)
@Tag("Webflow")
public class InitializeCaptchaActionTests {
    @Autowired
    @Qualifier("initializeCaptchaAction")
    private Action initializeCaptchaAction;

    @Test
    public void verifyCaptchaValidated() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        setRequestContext(context);
        setExternalContext(context.getExternalContext());
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, initializeCaptchaAction.execute(context).getId());
        assertNotNull(WebUtils.getRecaptchaSiteKey(context));
    }
}
