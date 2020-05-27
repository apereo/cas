package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.web.CaptchaValidator;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ValidateCaptchaActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = BaseCaptchaConfigurationTests.SharedTestConfiguration.class,
    properties = "cas.google-recaptcha.verify-url=http://localhost:9294"
)
@Tag("Webflow")
public class ValidateCaptchaActionTests {
    @Autowired
    @Qualifier("validateCaptchaAction")
    private Action validateCaptchaAction;

    @Test
    public void verifyCaptchaValidated() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();

        val data = "{\"success\": true }";
        request.addParameter(CaptchaValidator.REQUEST_PARAM_RECAPTCHA_RESPONSE, data);

        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        try (val webServer = new MockWebServer(9294,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val result = validateCaptchaAction.execute(context);
            assertNull(result);
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    @Test
    public void verifyCaptchaFails() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        try (val webServer = new MockWebServer(9305,
            new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();

            val props = new CasConfigurationProperties();
            props.getGoogleRecaptcha().setVerifyUrl("http://localhost:9305");
            val validateAction = new ValidateCaptchaAction(props.getGoogleRecaptcha());

            val result = validateAction.execute(context);
            assertNotNull(result);
            assertEquals(CasWebflowConstants.TRANSITION_ID_CAPTCHA_ERROR, result.getId());
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
