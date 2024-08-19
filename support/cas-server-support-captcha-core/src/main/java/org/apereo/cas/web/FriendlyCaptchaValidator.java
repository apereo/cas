package org.apereo.cas.web;

import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.hc.core5.http.HttpResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.HashMap;

/**
 * This is {@link FriendlyCaptchaValidator}.
 *
 * @author Jerome LELEU
 * @since 7.2.0
 */
public class FriendlyCaptchaValidator extends BaseCaptchaValidator {
    /**
     * Recaptcha token as a request parameter.
     */
    public static final String REQUEST_PARAM_FRIENDLY_CAPTCHA_RESPONSE = "frc-captcha-solution";

    @Getter
    private final String recaptchaResponseParameterName = REQUEST_PARAM_FRIENDLY_CAPTCHA_RESPONSE;

    public FriendlyCaptchaValidator(final GoogleRecaptchaProperties recaptchaProperties) {
        super(recaptchaProperties);
    }

    @Override
    @SneakyThrows
    protected HttpResponse executeCaptchaVerification(final String recaptchaResponse, final String userAgent) {
        val data = new HashMap<String, String>();
        data.put("siteKey", recaptchaProperties.getSiteKey());
        data.put("secret", recaptchaProperties.getSecret());
        data.put("solution", recaptchaResponse);

        val json = MAPPER.writeValueAsString(data);

        val exec = HttpExecutionRequest.builder()
                .method(HttpMethod.POST)
                .url(recaptchaProperties.getVerifyUrl())
                .headers(CollectionUtils.wrap("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .entity(json)
                .build();
        return HttpUtils.execute(exec);
    }
}
