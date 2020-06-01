package org.apereo.cas.web;

import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;

import java.nio.charset.StandardCharsets;

/**
 * This is {@link CaptchaValidator}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class CaptchaValidator {
    /**
     * Recaptcha response as a request parameter.
     */
    public static final String REQUEST_PARAM_RECAPTCHA_RESPONSE = "g-recaptcha-response";

    /**
     * Recaptcha token as a request parameter.
     */
    public static final String REQUEST_PARAM_RECAPTCHA_TOKEN = "g-recaptcha-token";

    private static final ObjectReader READER = new ObjectMapper().findAndRegisterModules().reader();

    private final String verifyUrl;

    private final String secret;

    private final double score;

    /**
     * Gets recaptcha response based on version.
     *
     * @param version the version
     * @param request the request
     * @return the recaptcha response
     */
    public static String getRecaptchaResponse(final GoogleRecaptchaProperties.RecaptchaVersions version,
                                              final HttpServletRequest request) {
        return version == GoogleRecaptchaProperties.RecaptchaVersions.V2
            ? request.getParameter(REQUEST_PARAM_RECAPTCHA_RESPONSE)
            : request.getParameter(REQUEST_PARAM_RECAPTCHA_TOKEN);
    }

    /**
     * Validate.
     *
     * @param recaptchaResponse the recaptcha response
     * @param userAgent         the user agent
     * @return true/false
     */
    public boolean validate(final String recaptchaResponse, final String userAgent) {
        HttpResponse response = null;
        try {
            response = HttpUtils.executePost(this.verifyUrl,
                CollectionUtils.wrap("secret", this.secret, "response", recaptchaResponse),
                CollectionUtils.wrap("User-Agent", userAgent, "Accept-Language", "en-US,en;q=0.5"));

            if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
                val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                if (StringUtils.isBlank(result)) {
                    throw new IllegalArgumentException("Unable to parse empty entity response from " + verifyUrl);
                }
                LOGGER.debug("Recaptcha verification response received: [{}]", result);
                val node = READER.readTree(result);
                if (node.has("score") && node.get("score").doubleValue() <= this.score) {
                    LOGGER.warn("Recaptcha score received is less than the threshold score defined for CAS");
                    return false;
                }
                if (node.has("success") && node.get("success").booleanValue()) {
                    LOGGER.trace("Recaptcha has successfully verified the request");
                    return true;
                }
            }
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        } finally {
            HttpUtils.close(response);
        }
        return false;
    }
}
