package org.apereo.cas.web;

import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.mock.web.MockHttpServletRequest;
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
    void verifyLowScore() throws Throwable {
        val secret = UUID.randomUUID().toString();
        val props = new GoogleRecaptchaProperties()
            .setScore(1)
            .setSecret(secret)
            .setVersion(GoogleRecaptchaProperties.RecaptchaVersions.GOOGLE_RECAPTCHA_V2)
            .setVerifyUrl("http://localhost:8812");
        val validator = CaptchaValidator.getInstance(props);

        val entity = MAPPER.writeValueAsString(Map.of("score", 0.5));
        try (val webServer = new MockWebServer(8812, entity)) {
            webServer.start();
            val response = UUID.randomUUID().toString();
            assertFalse(validator.validate(response, "Mozilla/5.0"));
        }
    }

    @Test
    void verifySuccess() throws Throwable {
        val props = new GoogleRecaptchaProperties()
            .setScore(0.1)
            .setSecret(UUID.randomUUID().toString())
            .setVersion(GoogleRecaptchaProperties.RecaptchaVersions.GOOGLE_RECAPTCHA_V2)
            .setVerifyUrl("http://localhost:8812");
        val validator = CaptchaValidator.getInstance(props);

        val entity = MAPPER.writeValueAsString(Map.of("score", 0.5, "success", true));
        try (val webServer = new MockWebServer(8812, entity)) {
            webServer.start();
            val response = UUID.randomUUID().toString();
            assertTrue(validator.validate(response, "Mozilla/5.0"));
        }
    }

    @Test
    void verifyBadResponse() throws Throwable {
        val secret = UUID.randomUUID().toString();
        val props = new GoogleRecaptchaProperties().setScore(0.1)
            .setVersion(GoogleRecaptchaProperties.RecaptchaVersions.GOOGLE_RECAPTCHA_V2)
            .setSecret(secret).setVerifyUrl("http://localhost:8812");
        val validator = CaptchaValidator.getInstance(props);
        try (val webServer = new MockWebServer(8812)) {
            webServer.start();
            val response = UUID.randomUUID().toString();
            assertFalse(validator.validate(response, "Mozilla/5.0"));
        }
    }

    @ParameterizedTest
    @EnumSource(GoogleRecaptchaProperties.RecaptchaVersions.class)
    void verifyInstance(final GoogleRecaptchaProperties.RecaptchaVersions version) throws Throwable {
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
