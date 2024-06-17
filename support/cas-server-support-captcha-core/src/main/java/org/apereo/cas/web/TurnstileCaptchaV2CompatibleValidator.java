package org.apereo.cas.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.LoggingUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

/**
 * This is {@link TurnstileCaptchaV2CompatibleValidator}.
 * Cloudflare Turnstile ReCAPTCHA v2 Compatible validator.
 * This version is the compatibility mode from Google ReCaptcha V2 with some differences.
 * <a href="https://developers.cloudflare.com/turnstile/migration/migrating-from-recaptcha/">migrating-from-recaptcha</a>
 * <a href="https://developers.cloudflare.com/turnstile/get-started/server-side-validation/">server-side-validation</a>
 *
 * @author KambaAbi
 */
@RequiredArgsConstructor
@Slf4j
public class TurnstileCaptchaV2CompatibleValidator implements CaptchaValidator {
    private static final ObjectReader READER = new ObjectMapper().findAndRegisterModules().reader();

    @Getter
    private final GoogleRecaptchaProperties recaptchaProperties;

    @Override
    public boolean validate(final String recaptchaResponse, final String userAgent) {
        HttpResponse response = null;
        try {
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .method(HttpMethod.POST)
                .url(recaptchaProperties.getVerifyUrl())
                .headers(CollectionUtils.wrap("User-Agent", userAgent, "Accept-Language", "en-US,en;q=0.5","Content-Type", "application/x-www-form-urlencoded"))
                // Sending query parameters like recaptcha doesn't work with turnstile. Needs content type header with entity generation below.
                // https://developers.cloudflare.com/turnstile/migration/migrating-from-recaptcha/#server-side-integration
                .entity("secret=" + recaptchaProperties.getSecret() + "&response" + recaptchaResponse)
                .build();

            response = HttpUtils.execute(exec);
            if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
                try (val content = response.getEntity().getContent()) {
                    val result = IOUtils.toString(content, StandardCharsets.UTF_8);
                    if (StringUtils.isBlank(result)) {
                        throw new IllegalArgumentException("Unable to parse empty entity response from " + recaptchaProperties.getVerifyUrl());
                    }
                    LOGGER.debug("Recaptcha verification response received: [{}]", result);
                    val node = READER.readTree(result);
                    if (node.has("score") && node.get("score").doubleValue() <= recaptchaProperties.getScore()) {
                        LOGGER.warn("Recaptcha score received is less than the threshold score defined for CAS");
                        return false;
                    }
                    if (node.has("success") && node.get("success").booleanValue()) {
                        LOGGER.trace("Recaptcha has successfully verified the request");
                        return true;
                    }
                }
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return false;
    }

    /**
     * Turnstile recaptcha v2 compatibility mode uses the same name as recaptcha v2's.
     */
    @Override
    public String getRecaptchaResponse(HttpServletRequest request) {
        return request.getParameter(GoogleCaptchaV2Validator.REQUEST_PARAM_RECAPTCHA_RESPONSE);
    }
}
