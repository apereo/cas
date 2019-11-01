package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.web.support.WebUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link ValidateCaptchaAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class ValidateCaptchaAction extends AbstractAction {
    /**
     * Recaptcha response as a request parameter.
     */
    public static final String REQUEST_PARAM_RECAPTCHA_RESPONSE = "g-recaptcha-response";

    /**
     * Recaptcha token as a request parameter.
     */
    public static final String REQUEST_PARAM_RECAPTCHA_TOKEN = "g-recaptcha-token";

    /**
     * Captcha error event.
     */
    public static final String EVENT_ID_ERROR = "captchaError";

    private static final ObjectReader READER = new ObjectMapper().findAndRegisterModules().reader();

    private final GoogleRecaptchaProperties recaptchaProperties;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val userAgent = WebUtils.getHttpServletRequestUserAgentFromRequestContext();

        val gRecaptchaResponse = getRecaptchaResponse(request);

        if (StringUtils.isBlank(gRecaptchaResponse)) {
            LOGGER.warn("Recaptcha response/token is missing from the request");
            return getError(requestContext);
        }

        HttpResponse response = null;
        try {
            response = HttpUtils.executePost(recaptchaProperties.getVerifyUrl(),
                CollectionUtils.wrap("secret", recaptchaProperties.getSecret(), "response", gRecaptchaResponse),
                CollectionUtils.wrap("User-Agent", userAgent, "Accept-Language", "en-US,en;q=0.5"));

            if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
                val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                LOGGER.debug("Recaptcha verification response received: [{}]", result);
                val node = READER.readTree(result);
                if (node.has("score") && node.get("score").doubleValue() <= recaptchaProperties.getScore()) {
                    LOGGER.warn("Recaptcha score received is less than the threshold score defined for CAS");
                    return getError(requestContext);
                }
                if (node.has("success") && node.get("success").booleanValue()) {
                    LOGGER.trace("Recaptcha has successfully verified the request");
                    return null;
                }
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            HttpUtils.close(response);
        }
        return getError(requestContext);
    }

    /**
     * Gets recaptcha response.
     *
     * @param request the request
     * @return the recaptcha response
     */
    protected String getRecaptchaResponse(final HttpServletRequest request) {
        return recaptchaProperties.getVersion() == GoogleRecaptchaProperties.RecaptchaVersions.V2
            ? request.getParameter(REQUEST_PARAM_RECAPTCHA_RESPONSE)
            : request.getParameter(REQUEST_PARAM_RECAPTCHA_TOKEN);
    }

    private Event getError(final RequestContext requestContext) {
        val messageContext = requestContext.getMessageContext();
        messageContext.addMessage(new MessageBuilder().error().code(EVENT_ID_ERROR).defaultText(EVENT_ID_ERROR).build());
        return getEventFactorySupport().event(this, EVENT_ID_ERROR);
    }
}
