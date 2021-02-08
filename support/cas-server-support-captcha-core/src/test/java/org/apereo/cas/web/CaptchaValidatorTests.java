package org.apereo.cas.web;

import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
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
public class CaptchaValidatorTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    public void verifyLowScore() throws Exception {
        val secret = UUID.randomUUID().toString();
        val props = new GoogleRecaptchaProperties().setScore(1).setSecret(secret).setVerifyUrl("http://localhost:8812");
        val validator = new GoogleCaptchaV2Validator(props);

        val entity = MAPPER.writeValueAsString(Map.of("score", .5));
        try (val webServer = new MockWebServer(8812,
            new ByteArrayResource(entity.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            val response = UUID.randomUUID().toString();
            assertFalse(validator.validate(response, "Mozilla/5.0"));
        }
    }

    @Test
    public void verifySuccess() throws Exception {
        val props = new GoogleRecaptchaProperties()
            .setScore(.1)
            .setSecret(UUID.randomUUID().toString())
            .setVerifyUrl("http://localhost:8812");
        val validator = new GoogleCaptchaV2Validator(props);

        val entity = MAPPER.writeValueAsString(Map.of("score", .5, "success", true));
        try (val webServer = new MockWebServer(8812,
            new ByteArrayResource(entity.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            val response = UUID.randomUUID().toString();
            assertTrue(validator.validate(response, "Mozilla/5.0"));
        }
    }

    @Test
    public void verifyBadResponse() {
        val secret = UUID.randomUUID().toString();
        val props = new GoogleRecaptchaProperties().setScore(1).setSecret(secret).setVerifyUrl("http://localhost:8812");
        val validator = new GoogleCaptchaV2Validator(props);
        try (val webServer = new MockWebServer(8812,
            new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            val response = UUID.randomUUID().toString();
            assertFalse(validator.validate(response, "Mozilla/5.0"));
        }
    }

    @Test
    public void verifyInstance() {
        assertNotNull(CaptchaValidator.getInstance(new GoogleRecaptchaProperties()
            .setVersion(GoogleRecaptchaProperties.RecaptchaVersions.GOOGLE_RECAPTCHA_V2)
            .setVerifyUrl("http://localhost:8812")));

        assertNotNull(CaptchaValidator.getInstance(new GoogleRecaptchaProperties()
            .setVersion(GoogleRecaptchaProperties.RecaptchaVersions.GOOGLE_RECAPTCHA_V3)
            .setVerifyUrl("http://localhost:8812")));

        assertNotNull(CaptchaValidator.getInstance(new GoogleRecaptchaProperties()
            .setVersion(GoogleRecaptchaProperties.RecaptchaVersions.HCAPTCHA)
            .setVerifyUrl("http://localhost:8812")));
    }
}
