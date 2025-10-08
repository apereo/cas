package org.apereo.cas.web;

import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.mock.web.MockHttpServletRequest;
import tools.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CaptchaValidatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("RestfulApi")
class CaptchaValidatorTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyLowScore() {
        val entity = MAPPER.writeValueAsString(Map.of("score", 0.5));
        try (val webServer = new MockWebServer(entity)) {
            webServer.start();
            val secret = UUID.randomUUID().toString();
            val props = new GoogleRecaptchaProperties()
                .setScore(1)
                .setSecret(secret)
                .setVersion(GoogleRecaptchaProperties.RecaptchaVersions.GOOGLE_RECAPTCHA_V2)
                .setVerifyUrl("http://localhost:%s".formatted(webServer.getPort()));
            val validator = CaptchaValidator.getInstance(props);
            val response = UUID.randomUUID().toString();
            assertFalse(validator.validate(response, "Mozilla/5.0"));
        }
    }

    

    @Test
    void verifySuccess() {
        val entity = MAPPER.writeValueAsString(Map.of("score", 0.5, "success", true));
        try (val webServer = new MockWebServer(entity)) {
            webServer.start();
            val props = new GoogleRecaptchaProperties()
                .setScore(0.1)
                .setSecret(UUID.randomUUID().toString())
                .setVersion(GoogleRecaptchaProperties.RecaptchaVersions.GOOGLE_RECAPTCHA_V2)
                .setVerifyUrl("http://localhost:%s".formatted(webServer.getPort()));
            val validator = CaptchaValidator.getInstance(props);
            val response = UUID.randomUUID().toString();
            assertTrue(validator.validate(response, "Mozilla/5.0"));
        }
    }

    @Test
    void verifyBadResponse() {
        try (val webServer = new MockWebServer()) {
            webServer.start();
            val secret = UUID.randomUUID().toString();
            val props = new GoogleRecaptchaProperties()
                .setScore(0.1)
                .setVersion(GoogleRecaptchaProperties.RecaptchaVersions.GOOGLE_RECAPTCHA_V2)
                .setSecret(secret)
                .setVerifyUrl("http://localhost:%s".formatted(webServer.getPort()));
            val validator = CaptchaValidator.getInstance(props);
            val response = UUID.randomUUID().toString();
            assertFalse(validator.validate(response, "Mozilla/5.0"));
        }
    }

    @ParameterizedTest
    @EnumSource(GoogleRecaptchaProperties.RecaptchaVersions.class)
    void verifyInstance(final GoogleRecaptchaProperties.RecaptchaVersions version) {
        val googleRecaptcha = new GoogleRecaptchaProperties()
            .setSiteKey(UUID.randomUUID().toString())
            .setSecret(UUID.randomUUID().toString())
            .setVersion(version).setVerifyUrl("http://localhost:8812");
        val validator = CaptchaValidator.getInstance(googleRecaptcha);
        assertNotNull(validator);
        assertNotNull(validator.getRecaptchaResponseParameterName());

        val request = new MockHttpServletRequest();
        val captchaResponse = UUID.randomUUID().toString();
        request.addParameter(validator.getRecaptchaResponseParameterName(), captchaResponse);
        assertNotNull(validator.getRecaptchaResponse(request));
    }
}
