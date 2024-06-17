package org.apereo.cas.web;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;
import lombok.val;
import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * This is {@link CloudFlareTurnstileCaptchaValidatorTests}.
 *
 * @author KambaAbi
 */
@Tag("Simple")
public class CloudFlareTurnstileCaptchaValidatorTests {
    @Test
    public void verifyOperation() {
        val props = new GoogleRecaptchaProperties()
            .setScore(.1)
            .setSecret(UUID.randomUUID().toString())
            .setVerifyUrl("http://localhost:8812");
        val validator = new TurnstileCaptchaV2CompatibleValidator(props);
        val request = new MockHttpServletRequest();
        request.addParameter(GoogleCaptchaV2Validator.REQUEST_PARAM_RECAPTCHA_RESPONSE, UUID.randomUUID().toString());
        assertNotNull(validator.getRecaptchaResponse(request));
    }
}
