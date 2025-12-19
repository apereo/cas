package org.apereo.cas.web;

import module java.base;
import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.util.MockWebServer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
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
    void verifyOperation() {
        try (val webServer = new MockWebServer(HttpStatus.OK, "{\"success\": true}")) {
            webServer.start();
            val response = UUID.randomUUID().toString();
            val props = new GoogleRecaptchaProperties()
                .setSecret(response)
                .setVerifyUrl("http://localhost:%s".formatted(webServer.getPort()));
            val validator = new FriendlyCaptchaValidator(props);
            val request = new MockHttpServletRequest();
            request.addParameter(FriendlyCaptchaValidator.REQUEST_PARAM_FRIENDLY_CAPTCHA_RESPONSE, response);
            assertNotNull(validator.getRecaptchaResponse(request));
            assertTrue(validator.validate(response, "Mozilla"));
        }
    }
}
