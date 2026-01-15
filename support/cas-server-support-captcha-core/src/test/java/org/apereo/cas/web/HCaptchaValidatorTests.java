package org.apereo.cas.web;

import module java.base;
import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link HCaptchaValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Simple")
class HCaptchaValidatorTests {
    @Test
    void verifyOperation() {
        val props = new GoogleRecaptchaProperties()
            .setScore(0.1)
            .setSecret(UUID.randomUUID().toString())
            .setVerifyUrl("http://localhost:8812");
        val validator = new HCaptchaValidator(props);
        val request = new MockHttpServletRequest();
        request.addParameter(HCaptchaValidator.REQUEST_PARAM_HCAPTCHA_RESPONSE, UUID.randomUUID().toString());
        assertNotNull(validator.getRecaptchaResponse(request));
    }
}
