package org.apereo.cas.web;

import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link BaseCaptchaValidator}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Getter
@RequiredArgsConstructor
@Slf4j
public abstract class BaseCaptchaValidator implements CaptchaValidator {
    protected static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    protected final GoogleRecaptchaProperties recaptchaProperties;

    @Override
    public boolean validate(final String recaptchaResponse, final String userAgent) {
        HttpResponse response = null;
        try {
            response = executeCaptchaVerification(recaptchaResponse, userAgent);
            if (response != null && response.getCode() == HttpStatus.OK.value()) {
                return parseCaptchaResponse(response);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return false;
    }

    @Override
    public String getRecaptchaResponse(final HttpServletRequest request) {
        return request.getParameter(getRecaptchaResponseParameterName());
    }
    
    protected HttpResponse executeCaptchaVerification(final String recaptchaResponse, final String userAgent) {
        val headers = CollectionUtils.<String, String>wrap(
            HttpHeaders.USER_AGENT, userAgent,
            HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            "Accept-Language", "en-US,en;q=0.5");
        headers.putAll(recaptchaProperties.getHeaders());
        val exec = HttpExecutionRequest.builder()
            .method(HttpMethod.POST)
            .url(recaptchaProperties.getVerifyUrl())
            .headers(headers)
            .entity("secret=%s&response=%s".formatted(recaptchaProperties.getSecret(), recaptchaResponse))
            .build();
        return HttpUtils.execute(exec);
    }

    protected boolean parseCaptchaResponse(final HttpResponse response) throws Exception {
        try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
            val result = IOUtils.toString(content, StandardCharsets.UTF_8);
            if (StringUtils.isBlank(result)) {
                throw new IllegalArgumentException("Unable to parse empty entity response from " + recaptchaProperties.getVerifyUrl());
            }
            LOGGER.debug("Recaptcha verification response received: [{}]", result);
            val node = MAPPER.reader().readTree(result);
            if (node.has("score") && node.get("score").doubleValue() <= recaptchaProperties.getScore()) {
                LOGGER.warn("Recaptcha score received is less than the threshold score defined for CAS");
                return false;
            }
            if (node.has("success") && node.get("success").booleanValue()) {
                LOGGER.trace("Recaptcha has successfully verified the request");
                return true;
            }
        }
        return false;
    }
}
