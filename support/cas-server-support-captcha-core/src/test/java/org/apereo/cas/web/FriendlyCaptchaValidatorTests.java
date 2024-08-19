package org.apereo.cas.web;

import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link FriendlyCaptchaValidatorTests}.
 *
 * @author Jerome LELEU
 * @since 7.2.0
 */
@Tag("Simple")
class FriendlyCaptchaValidatorTests {
    @Test
    void verifyOperation() throws Throwable {
        val props = new GoogleRecaptchaProperties()
            .setSecret(UUID.randomUUID().toString())
            .setVerifyUrl("http://localhost:8812");
        val validator = new FriendlyCaptchaValidator(props);
        val request = new MockHttpServletRequest();
        request.addParameter(FriendlyCaptchaValidator.REQUEST_PARAM_FRIENDLY_CAPTCHA_RESPONSE, UUID.randomUUID().toString());
        assertNotNull(validator.getRecaptchaResponse(request));
    }
}
